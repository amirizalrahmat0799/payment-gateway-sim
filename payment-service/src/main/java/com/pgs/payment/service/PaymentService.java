package com.pgs.payment.service;

import com.pgs.common.event.PaymentEvent;
import com.pgs.common.event.PaymentEventType;
import com.pgs.payment.client.MerchantClient.MerchantContext;
import com.pgs.payment.client.TokenizationClient;
import com.pgs.payment.domain.CardSnapshot;
import com.pgs.payment.domain.IdempotencyRecord;
import com.pgs.payment.domain.IdempotencyRecordRepository;
import com.pgs.payment.domain.Payment;
import com.pgs.payment.domain.PaymentRepository;
import com.pgs.payment.domain.PaymentStatus;
import com.pgs.payment.domain.Refund;
import com.pgs.payment.domain.RefundRepository;
import com.pgs.payment.issuer.AuthorizationDecision;
import com.pgs.payment.issuer.IssuerSimulator;
import com.pgs.payment.outbox.OutboxWriter;
import com.pgs.payment.web.dto.CreatePaymentRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final RefundRepository refunds;
    private final IdempotencyRecordRepository idempotencyRecords;
    private final TokenizationClient tokenization;
    private final IssuerSimulator issuer;
    private final OutboxWriter outbox;
    private final TransactionTemplate tx;
    private final PaymentMetrics metrics;

    public PaymentService(PaymentRepository payments, RefundRepository refunds,
                          IdempotencyRecordRepository idempotencyRecords, TokenizationClient tokenization,
                          IssuerSimulator issuer, OutboxWriter outbox, TransactionTemplate tx,
                          PaymentMetrics metrics) {
        this.payments = payments;
        this.refunds = refunds;
        this.idempotencyRecords = idempotencyRecords;
        this.tokenization = tokenization;
        this.issuer = issuer;
        this.outbox = outbox;
        this.tx = tx;
        this.metrics = metrics;
    }

    public record CreateResult(Payment payment, boolean replayed) {
    }

    public record RefundResult(Refund refund, Payment payment) {
    }

    /**
     * Creates (authorizes, and optionally captures) a payment.
     * <p>
     * Idempotency flow:
     * <ol>
     *   <li>Same key + same body seen before → return the original payment, do nothing else.</li>
     *   <li>Same key + different body → reject (client bug).</li>
     *   <li>Two identical requests racing → the unique constraint lets exactly one insert win;
     *       the loser rolls back and replays the winner's result.</li>
     * </ol>
     * The call to tokenization-service happens outside the DB transaction so we don't hold a
     * connection open while waiting on the network.
     */
    public CreateResult create(MerchantContext merchant, String idempotencyKey, CreatePaymentRequest request) {
        String requestHash = RequestHasher.hash(request);

        Optional<CreateResult> replay = findReplay(merchant.merchantId(), idempotencyKey, requestHash);
        if (replay.isPresent()) {
            return replay.get();
        }

        CardSnapshot card = tokenization.lookup(request.cardToken());
        AuthorizationDecision decision = issuer.authorize(card, request.amount());

        try {
            CreateResult result = tx.execute(status -> {
                Payment payment = decision.approved()
                        ? Payment.authorized(merchant.merchantId(), request.amount(), request.currency(), card,
                        merchant.feeBps(), request.description(), decision.authCode())
                        : Payment.declined(merchant.merchantId(), request.amount(), request.currency(), card,
                        merchant.feeBps(), request.description(), decision.declineReason());

                Long captured = null;
                if (decision.approved() && request.captureNow()) {
                    captured = payment.capture(null);
                }
                payments.save(payment);
                if (captured != null) {
                    outbox.append(event(payment, PaymentEventType.CAPTURED, captured));
                }
                idempotencyRecords.saveAndFlush(
                        new IdempotencyRecord(merchant.merchantId(), idempotencyKey, requestHash, payment.getId()));
                return new CreateResult(payment, false);
            });
            recordCreated(result);
            return result;
        } catch (DataIntegrityViolationException raceLost) {
            return findReplay(merchant.merchantId(), idempotencyKey, requestHash).orElseThrow(() -> raceLost);
        }
    }

    @Transactional
    public Payment capture(MerchantContext merchant, UUID paymentId, Long amount) {
        Payment payment = load(merchant, paymentId);
        long captured = payment.capture(amount);
        outbox.append(event(payment, PaymentEventType.CAPTURED, captured));
        metrics.captured(payment.getCurrency(), captured);
        return payment;
    }

    @Transactional
    public Payment voidPayment(MerchantContext merchant, UUID paymentId) {
        Payment payment = load(merchant, paymentId);
        payment.voidAuthorization();
        return payment;
    }

    @Transactional
    public RefundResult refund(MerchantContext merchant, UUID paymentId, long amount) {
        Payment payment = load(merchant, paymentId);
        payment.refund(amount);
        Refund refund = refunds.save(new Refund(payment.getId(), amount));
        outbox.append(event(payment, PaymentEventType.REFUNDED, amount));
        metrics.refunded(payment.getCurrency(), amount);
        return new RefundResult(refund, payment);
    }

    @Transactional(readOnly = true)
    public Payment get(MerchantContext merchant, UUID paymentId) {
        return load(merchant, paymentId);
    }

    @Transactional(readOnly = true)
    public List<Refund> refundsOf(MerchantContext merchant, UUID paymentId) {
        load(merchant, paymentId);
        return refunds.findByPaymentIdOrderByCreatedAt(paymentId);
    }

    @Transactional(readOnly = true)
    public Page<Payment> list(MerchantContext merchant, int page, int size) {
        return payments.findByMerchantIdOrderByCreatedAtDesc(merchant.merchantId(),
                PageRequest.of(page, Math.min(size, 100)));
    }

    private void recordCreated(CreateResult result) {
        Payment p = result.payment();
        metrics.authorization(p.getStatus() != PaymentStatus.DECLINED);
        if (p.getCapturedAmount() > 0) {
            metrics.captured(p.getCurrency(), p.getCapturedAmount());
        }
    }

    private Optional<CreateResult> findReplay(UUID merchantId, String idempotencyKey, String requestHash) {
        return idempotencyRecords.findByMerchantIdAndIdempotencyKey(merchantId, idempotencyKey)
                .map(record -> {
                    if (!record.getRequestHash().equals(requestHash)) {
                        throw new IdempotencyConflictException(idempotencyKey);
                    }
                    Payment original = payments.findById(record.getPaymentId())
                            .orElseThrow(() -> new PaymentNotFoundException(record.getPaymentId()));
                    return new CreateResult(original, true);
                });
    }

    /** Payments are always looked up scoped to the calling merchant: no cross-merchant access. */
    private Payment load(MerchantContext merchant, UUID paymentId) {
        return payments.findByIdAndMerchantId(paymentId, merchant.merchantId())
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private static PaymentEvent event(Payment payment, PaymentEventType type, long amount) {
        return new PaymentEvent(UUID.randomUUID(), type, payment.getId(), payment.getMerchantId(),
                amount, payment.getCurrency(), payment.getFeeBps(), Instant.now());
    }
}
