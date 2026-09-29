package com.mizal.pgs.payment.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Business metrics, scraped by Prometheus from /actuator/prometheus:
 * <ul>
 *   <li>{@code pgs_payments_authorizations_total{outcome="approved|declined"}}</li>
 *   <li>{@code pgs_payments_captured_amount_minor_total{currency}}: money captured, in minor units</li>
 *   <li>{@code pgs_payments_refunded_amount_minor_total{currency}}</li>
 * </ul>
 */
@Component
public class PaymentMetrics {

    private final MeterRegistry registry;

    public PaymentMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void authorization(boolean approved) {
        Counter.builder("pgs.payments.authorizations")
                .description("Authorization attempts by outcome")
                .tag("outcome", approved ? "approved" : "declined")
                .register(registry)
                .increment();
    }

    public void captured(String currency, long amountMinor) {
        Counter.builder("pgs.payments.captured.amount.minor")
                .description("Captured amount in minor units")
                .tag("currency", currency)
                .register(registry)
                .increment(amountMinor);
    }

    public void refunded(String currency, long amountMinor) {
        Counter.builder("pgs.payments.refunded.amount.minor")
                .description("Refunded amount in minor units")
                .tag("currency", currency)
                .register(registry)
                .increment(amountMinor);
    }
}
