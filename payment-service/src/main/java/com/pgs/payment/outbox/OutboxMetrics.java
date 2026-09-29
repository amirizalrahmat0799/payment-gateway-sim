package com.pgs.payment.outbox;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Exposes {@code pgs_outbox_pending}: outbox rows waiting to be published to Kafka. */
@Component
class OutboxMetrics {

    OutboxMetrics(OutboxRepository outbox, MeterRegistry registry) {
        Gauge.builder("pgs.outbox.pending", outbox, repo -> repo.countByPublishedAtIsNull())
                .description("Outbox events not yet published to Kafka")
                .register(registry);
    }
}
