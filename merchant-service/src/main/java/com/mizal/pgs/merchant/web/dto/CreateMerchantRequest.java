package com.mizal.pgs.merchant.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param feeBps processing fee in basis points (250 = 2.50%)
 */
public record CreateMerchantRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email String email,
        @Min(0) @Max(1000) int feeBps) {
}
