package com.pgs.tokenization.card;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LuhnTest {

    @ParameterizedTest
    @ValueSource(strings = {"4242424242424242", "5555555555554444", "378282246310005", "4000000000000002"})
    void acceptsValidTestCards(String pan) {
        assertThat(Luhn.isValid(pan)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"4242424242424241", "1234567890123456", "4242", "42424242424242AB", ""})
    void rejectsInvalidNumbers(String pan) {
        assertThat(Luhn.isValid(pan)).isFalse();
    }
}
