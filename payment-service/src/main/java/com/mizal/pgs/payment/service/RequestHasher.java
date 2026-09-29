package com.mizal.pgs.payment.service;

import com.mizal.pgs.payment.web.dto.CreatePaymentRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Fingerprints a request body so a reused idempotency key with a different body can be detected. */
final class RequestHasher {

    private RequestHasher() {
    }

    static String hash(CreatePaymentRequest r) {
        String canonical = String.join("|",
                Long.toString(r.amount()),
                r.currency(),
                r.cardToken(),
                Boolean.toString(r.captureNow()),
                r.description() == null ? "" : r.description());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
