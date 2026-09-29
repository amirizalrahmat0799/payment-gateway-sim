package com.pgs.payment.web.dto;

import com.pgs.payment.domain.Refund;

import java.time.Instant;
import java.util.UUID;

public record RefundResponse(UUID id, UUID paymentId, long amount, Instant createdAt) {

    public static RefundResponse from(Refund r) {
        return new RefundResponse(r.getId(), r.getPaymentId(), r.getAmount(), r.getCreatedAt());
    }
}
