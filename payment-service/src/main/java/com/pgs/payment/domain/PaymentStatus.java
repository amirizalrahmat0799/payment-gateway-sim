package com.pgs.payment.domain;

/**
 * Payment lifecycle.
 *
 * <pre>
 *                ┌──────────► VOIDED
 *                │
 * AUTHORIZED ────┼──────────► CAPTURED ──► PARTIALLY_REFUNDED ──► REFUNDED
 *                                  └─────────────────────────────────▲
 * DECLINED  (terminal)
 * </pre>
 */
public enum PaymentStatus {
    AUTHORIZED,
    DECLINED,
    CAPTURED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    VOIDED;

    public boolean isRefundable() {
        return this == CAPTURED || this == PARTIALLY_REFUNDED;
    }
}
