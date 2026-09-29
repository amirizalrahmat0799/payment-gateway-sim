package com.pgs.tokenization.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenizeRequest(
        @NotBlank String pan,
        @Min(1) @Max(12) int expiryMonth,
        @Min(2000) @Max(2100) int expiryYear,
        @Size(max = 120) String cardholderName) {

    /** Never log the PAN, even by accident. */
    @Override
    public String toString() {
        return "TokenizeRequest[pan=****, expiry=" + expiryMonth + "/" + expiryYear + "]";
    }
}
