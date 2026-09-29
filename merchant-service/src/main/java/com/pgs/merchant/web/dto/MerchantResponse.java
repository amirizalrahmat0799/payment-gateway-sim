package com.pgs.merchant.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pgs.merchant.domain.Merchant;
import com.pgs.merchant.domain.MerchantStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MerchantResponse(
        UUID id,
        String name,
        String email,
        MerchantStatus status,
        int feeBps,
        String apiKeyPrefix,
        String apiKey,
        Instant createdAt) {

    public static MerchantResponse from(Merchant m) {
        return from(m, null);
    }

    /** Includes the plaintext API key; only used right after onboarding or rotation. */
    public static MerchantResponse from(Merchant m, String apiKey) {
        return new MerchantResponse(m.getId(), m.getName(), m.getEmail(), m.getStatus(),
                m.getFeeBps(), m.getApiKeyPrefix(), apiKey, m.getCreatedAt());
    }
}
