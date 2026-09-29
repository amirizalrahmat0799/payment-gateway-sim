package com.pgs.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Money-movement event published by payment-service through the transactional outbox
 * and consumed by settlement-service.
 *
 * @param eventId     unique id, used by consumers for de-duplication (at-least-once delivery)
 * @param type        CAPTURED or REFUNDED
 * @param paymentId   payment the event belongs to
 * @param merchantId  merchant that owns the payment
 * @param amount      amount in minor units (e.g. sen / cents)
 * @param currency    ISO-4217 currency code
 * @param feeBps      merchant fee in basis points at the time of payment
 * @param occurredAt  when the state change happened
 */
public record PaymentEvent(
        UUID eventId,
        PaymentEventType type,
        UUID paymentId,
        UUID merchantId,
        long amount,
        String currency,
        int feeBps,
        Instant occurredAt) {

    public String topic() {
        return switch (type) {
            case CAPTURED -> Topics.PAYMENT_CAPTURED;
            case REFUNDED -> Topics.PAYMENT_REFUNDED;
        };
    }
}
