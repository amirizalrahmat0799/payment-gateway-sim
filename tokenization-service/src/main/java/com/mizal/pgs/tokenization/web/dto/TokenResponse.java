package com.mizal.pgs.tokenization.web.dto;

import com.mizal.pgs.tokenization.card.CardBrand;
import com.mizal.pgs.tokenization.vault.CardToken;

/** Safe card metadata: never contains the PAN. */
public record TokenResponse(
        String token,
        CardBrand brand,
        String last4,
        int expiryMonth,
        int expiryYear,
        String fingerprint,
        boolean expired) {

    public static TokenResponse from(CardToken t, boolean expired) {
        return new TokenResponse(t.getToken(), t.getBrand(), t.getLast4(),
                t.getExpiryMonth(), t.getExpiryYear(), t.getFingerprint(), expired);
    }
}
