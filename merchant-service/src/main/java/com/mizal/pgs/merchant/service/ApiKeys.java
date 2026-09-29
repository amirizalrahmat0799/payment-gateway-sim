package com.mizal.pgs.merchant.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Generates and hashes merchant API keys.
 * <p>
 * Only the SHA-256 hash is stored. Because keys are 256-bit random values, a fast hash is
 * sufficient (no need for bcrypt): brute-forcing the key space is infeasible.
 */
public final class ApiKeys {

    private static final String PREFIX = "sk_test_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ApiKeys() {
    }

    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** First characters shown in dashboards so a key can be identified without revealing it. */
    public static String displayPrefix(String apiKey) {
        return apiKey.substring(0, Math.min(apiKey.length(), PREFIX.length() + 4));
    }

    public static String hash(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(apiKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
