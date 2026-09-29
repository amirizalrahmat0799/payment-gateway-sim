package com.mizal.pgs.merchant.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeysTest {

    @Test
    void generatesUniqueKeysWithTestPrefix() {
        String a = ApiKeys.generate();
        String b = ApiKeys.generate();

        assertThat(a).startsWith("sk_test_").isNotEqualTo(b);
        assertThat(a).hasSizeGreaterThan(40);
    }

    @Test
    void hashIsDeterministicAndHex() {
        String key = ApiKeys.generate();

        assertThat(ApiKeys.hash(key)).isEqualTo(ApiKeys.hash(key)).matches("[0-9a-f]{64}");
    }

    @Test
    void displayPrefixDoesNotRevealWholeKey() {
        String key = ApiKeys.generate();

        assertThat(ApiKeys.displayPrefix(key)).hasSize(12).isNotEqualTo(key);
    }
}
