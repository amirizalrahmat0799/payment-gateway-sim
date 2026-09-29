package com.mizal.pgs.tokenization.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardCryptoTest {

    private static final String KEY = "corJQ8t0A3/fSmfWZTgYpXQm+fzLqwomJcj7DnEbxW4=";
    private static final String FP_KEY = "SZpjUY1zPCZLglePybkXidyWVtFnnKTUxQw54Z65UL0=";

    private final CardCrypto crypto = new CardCrypto(new VaultProperties(KEY, FP_KEY));

    @Test
    void encryptThenDecryptRoundTrips() {
        String encrypted = crypto.encrypt("4242424242424242");

        assertThat(encrypted).doesNotContain("4242");
        assertThat(crypto.decrypt(encrypted)).isEqualTo("4242424242424242");
    }

    @Test
    void sameInputEncryptsDifferentlyEachTime() {
        assertThat(crypto.encrypt("4242424242424242")).isNotEqualTo(crypto.encrypt("4242424242424242"));
    }

    @Test
    void fingerprintIsStableForSameCard() {
        assertThat(crypto.fingerprint("4242424242424242"))
                .isEqualTo(crypto.fingerprint("4242424242424242"))
                .isNotEqualTo(crypto.fingerprint("5555555555554444"));
    }

    @Test
    void tamperedCiphertextIsRejected() {
        String encrypted = crypto.encrypt("4242424242424242");
        char[] chars = encrypted.toCharArray();
        chars[20] = chars[20] == 'A' ? 'B' : 'A';

        assertThatThrownBy(() -> crypto.decrypt(new String(chars))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsWrongKeyLength() {
        assertThatThrownBy(() -> new CardCrypto(new VaultProperties("c2hvcnQ=", FP_KEY)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
