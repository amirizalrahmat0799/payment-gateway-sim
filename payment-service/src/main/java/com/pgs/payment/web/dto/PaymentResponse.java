package com.pgs.payment.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pgs.payment.domain.Payment;
import com.pgs.payment.domain.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        PaymentStatus status,
        long amount,
        String currency,
        long capturedAmount,
        long refundedAmount,
        long refundableAmount,
        Card card,
        String authCode,
        String declineReason,
        String description,
        Instant createdAt,
        Instant updatedAt) {

    public record Card(String token, String brand, String last4) {
    }

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getStatus(), p.getAmount(), p.getCurrency(),
                p.getCapturedAmount(), p.getRefundedAmount(), p.refundableAmount(),
                new Card(p.getCardToken(), p.getCardBrand(), p.getCardLast4()),
                p.getAuthCode(), p.getDeclineReason(), p.getDescription(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
