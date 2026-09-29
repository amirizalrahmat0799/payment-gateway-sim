package com.mizal.pgs.payment.service;

import com.mizal.pgs.common.event.PaymentEvent;
import com.mizal.pgs.common.event.PaymentEventType;
import com.mizal.pgs.payment.client.MerchantClient.MerchantContext;
import com.mizal.pgs.payment.client.TokenizationClient;
import com.mizal.pgs.payment.domain.CardSnapshot;
import com.mizal.pgs.payment.domain.IdempotencyRecord;
import com.mizal.pgs.payment.domain.IdempotencyRecordRepository;
import com.mizal.pgs.payment.domain.Payment;
import com.mizal.pgs.payment.domain.PaymentRepository;
import com.mizal.pgs.payment.domain.PaymentStatus;
import com.mizal.pgs.payment.domain.RefundRepository;
import com.mizal.pgs.payment.issuer.IssuerSimulator;
import com.mizal.pgs.payment.outbox.OutboxWriter;
import com.mizal.pgs.payment.web.dto.CreatePaymentRequest;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final MerchantContext MERCHANT = new MerchantContext(UUID.randomUUID(), "Kedai Kopi", 250);
    private static final CardSnapshot CARD = new CardSnapshot("tok_abc", "VISA", "4242", false);

    @Mock
    PaymentRepository payments;
    @Mock
    RefundRepository refunds;
    @Mock
    IdempotencyRecordRepository idempotencyRecords;
    @Mock
    TokenizationClient tokenization;
    @Mock
    OutboxWriter outbox;
    @Mock
    TransactionTemplate tx;

    PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(payments, refunds, idempotencyRecords, tokenization,
                new IssuerSimulator(5_000_000), outbox, tx,
                new PaymentMetrics(new SimpleMeterRegistry()));
    }

    @SuppressWarnings("unchecked")
    private void runTransactionsInline() {
        when(tx.execute(any())).thenAnswer(inv -> ((TransactionCallback<Object>) inv.getArgument(0)).doInTransaction(null));
    }

    @Test
    void authorizeAndCaptureWritesCapturedEventToOutbox() {
        runTransactionsInline();
        when(idempotencyRecords.findByMerchantIdAndIdempotencyKey(MERCHANT.merchantId(), "key-00001"))
                .thenReturn(Optional.empty());
        when(tokenization.lookup("tok_abc")).thenReturn(CARD);

        var result = service.create(MERCHANT, "key-00001", new CreatePaymentRequest(10_000, "MYR", "tok_abc", true, null));

        assertThat(result.replayed()).isFalse();
        assertThat(result.payment().getStatus()).isEqualTo(PaymentStatus.CAPTURED);

        ArgumentCaptor<PaymentEvent> event = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(outbox).append(event.capture());
        assertThat(event.getValue().type()).isEqualTo(PaymentEventType.CAPTURED);
        assertThat(event.getValue().amount()).isEqualTo(10_000);
        assertThat(event.getValue().feeBps()).isEqualTo(250);
    }

    @Test
    void authorizeOnlyDoesNotEmitEvent() {
        runTransactionsInline();
        when(idempotencyRecords.findByMerchantIdAndIdempotencyKey(any(), anyString())).thenReturn(Optional.empty());
        when(tokenization.lookup("tok_abc")).thenReturn(CARD);

        var result = service.create(MERCHANT, "key-00002", new CreatePaymentRequest(10_000, "MYR", "tok_abc", false, null));

        assertThat(result.payment().getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        verify(outbox, never()).append(any());
    }

    @Test
    void replaysOriginalPaymentForSameKeyAndBody() {
        var request = new CreatePaymentRequest(10_000, "MYR", "tok_abc", true, "order-42");
        Payment original = Payment.authorized(MERCHANT.merchantId(), 10_000, "MYR", CARD, 250, "order-42", "AB12CD");
        var record = new IdempotencyRecord(MERCHANT.merchantId(), "key-00003", RequestHasher.hash(request), original.getId());

        when(idempotencyRecords.findByMerchantIdAndIdempotencyKey(MERCHANT.merchantId(), "key-00003"))
                .thenReturn(Optional.of(record));
        when(payments.findById(eq(original.getId()))).thenReturn(Optional.of(original));

        var result = service.create(MERCHANT, "key-00003", request);

        assertThat(result.replayed()).isTrue();
        assertThat(result.payment()).isSameAs(original);
        verifyNoInteractions(tokenization, outbox, tx);
    }

    @Test
    void rejectsSameKeyWithDifferentBody() {
        var firstRequest = new CreatePaymentRequest(10_000, "MYR", "tok_abc", true, null);
        var record = new IdempotencyRecord(MERCHANT.merchantId(), "key-00004", RequestHasher.hash(firstRequest), UUID.randomUUID());
        when(idempotencyRecords.findByMerchantIdAndIdempotencyKey(MERCHANT.merchantId(), "key-00004"))
                .thenReturn(Optional.of(record));

        var differentAmount = new CreatePaymentRequest(99_999, "MYR", "tok_abc", true, null);

        assertThatThrownBy(() -> service.create(MERCHANT, "key-00004", differentAmount))
                .isInstanceOf(IdempotencyConflictException.class);
        verifyNoInteractions(tokenization);
    }
}
