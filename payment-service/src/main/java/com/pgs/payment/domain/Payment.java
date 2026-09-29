package com.pgs.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment aggregate. All state transitions go through methods on this class so the
 * business rules (what can be captured, how much can be refunded) live in one place.
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "captured_amount", nullable = false)
    private long capturedAmount;

    @Column(name = "refunded_amount", nullable = false)
    private long refundedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "card_token", nullable = false)
    private String cardToken;

    @Column(name = "card_brand", nullable = false)
    private String cardBrand;

    @Column(name = "card_last4", nullable = false)
    private String cardLast4;

    @Column(name = "fee_bps", nullable = false)
    private int feeBps;

    @Column(name = "auth_code")
    private String authCode;

    @Column(name = "decline_reason")
    private String declineReason;

    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: two concurrent captures/refunds cannot both win. */
    @Version
    private Long version;

    protected Payment() {
    }

    private Payment(UUID merchantId, long amount, String currency, CardSnapshot card, int feeBps, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.id = UUID.randomUUID();
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.cardToken = card.token();
        this.cardBrand = card.brand();
        this.cardLast4 = card.last4();
        this.feeBps = feeBps;
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public static Payment authorized(UUID merchantId, long amount, String currency, CardSnapshot card,
                                     int feeBps, String description, String authCode) {
        Payment p = new Payment(merchantId, amount, currency, card, feeBps, description);
        p.status = PaymentStatus.AUTHORIZED;
        p.authCode = authCode;
        return p;
    }

    public static Payment declined(UUID merchantId, long amount, String currency, CardSnapshot card,
                                   int feeBps, String description, String reason) {
        Payment p = new Payment(merchantId, amount, currency, card, feeBps, description);
        p.status = PaymentStatus.DECLINED;
        p.declineReason = reason;
        return p;
    }

    /**
     * Captures the authorization. A partial capture releases the remainder of the hold.
     *
     * @param captureAmount amount to capture, or {@code null} for the full authorized amount
     */
    public long capture(Long captureAmount) {
        requireStatus(PaymentStatus.AUTHORIZED, "capture");
        long toCapture = captureAmount == null ? amount : captureAmount;
        if (toCapture <= 0 || toCapture > amount) {
            throw new InvalidPaymentOperationException(
                    "Capture amount must be between 1 and the authorized amount (" + amount + ")");
        }
        this.capturedAmount = toCapture;
        transitionTo(PaymentStatus.CAPTURED);
        return toCapture;
    }

    public void voidAuthorization() {
        requireStatus(PaymentStatus.AUTHORIZED, "void");
        transitionTo(PaymentStatus.VOIDED);
    }

    public void refund(long refundAmount) {
        if (!status.isRefundable()) {
            throw new InvalidPaymentOperationException("Cannot refund a payment in status " + status);
        }
        if (refundAmount <= 0 || refundAmount > refundableAmount()) {
            throw new InvalidPaymentOperationException(
                    "Refund amount must be between 1 and the refundable amount (" + refundableAmount() + ")");
        }
        this.refundedAmount += refundAmount;
        transitionTo(refundedAmount == capturedAmount ? PaymentStatus.REFUNDED : PaymentStatus.PARTIALLY_REFUNDED);
    }

    public long refundableAmount() {
        return capturedAmount - refundedAmount;
    }

    private void requireStatus(PaymentStatus expected, String operation) {
        if (status != expected) {
            throw new InvalidPaymentOperationException(
                    "Cannot " + operation + " a payment in status " + status + " (expected " + expected + ")");
        }
    }

    private void transitionTo(PaymentStatus next) {
        this.status = next;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public long getCapturedAmount() {
        return capturedAmount;
    }

    public long getRefundedAmount() {
        return refundedAmount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getCardToken() {
        return cardToken;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public int getFeeBps() {
        return feeBps;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getDeclineReason() {
        return declineReason;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
