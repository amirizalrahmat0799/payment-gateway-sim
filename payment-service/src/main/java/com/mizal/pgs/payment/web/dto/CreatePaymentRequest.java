package com.mizal.pgs.payment.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * @param amount  amount in minor units (e.g. 1050 = RM 10.50)
 * @param capture capture immediately after a successful authorization (default false)
 */
public record CreatePaymentRequest(
        @Positive long amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be an ISO-4217 code such as MYR") String currency,
        @NotBlank String cardToken,
        Boolean capture,
        @Size(max = 255) String description) {

    public boolean captureNow() {
        return Boolean.TRUE.equals(capture);
    }
}
