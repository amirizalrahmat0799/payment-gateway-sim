package com.pgs.common.event;

/**
 * Kafka topic names shared by producers and consumers.
 */
public final class Topics {

    public static final String PAYMENT_CAPTURED = "payments.captured";
    public static final String PAYMENT_REFUNDED = "payments.refunded";

    private Topics() {
    }
}
