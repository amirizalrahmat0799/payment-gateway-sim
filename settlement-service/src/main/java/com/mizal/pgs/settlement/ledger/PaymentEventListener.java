package com.mizal.pgs.settlement.ledger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mizal.pgs.common.event.PaymentEvent;
import com.mizal.pgs.common.event.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    private final LedgerRepository ledger;
    private final ObjectMapper objectMapper;

    public PaymentEventListener(LedgerRepository ledger, ObjectMapper objectMapper) {
        this.ledger = ledger;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {Topics.PAYMENT_CAPTURED, Topics.PAYMENT_REFUNDED})
    public void onPaymentEvent(String payload) throws JsonProcessingException {
        PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);
        if (ledger.record(event)) {
            log.info("Ledger: {} {} {} for payment {}", event.type(), event.amount(), event.currency(), event.paymentId());
        } else {
            log.info("Ledger: duplicate event {} ignored", event.eventId());
        }
    }
}
