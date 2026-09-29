package com.mizal.pgs.tokenization.crypto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param encryptionKey  base64-encoded 256-bit AES key for encrypting PANs at rest
 * @param fingerprintKey base64-encoded HMAC key for card fingerprints
 */
@Validated
@ConfigurationProperties(prefix = "vault")
public record VaultProperties(@NotBlank String encryptionKey, @NotBlank String fingerprintKey) {
}
