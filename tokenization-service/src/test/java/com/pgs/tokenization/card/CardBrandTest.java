package com.pgs.tokenization.card;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CardBrandTest {

    @ParameterizedTest
    @CsvSource({
            "4242424242424242, VISA",
            "5555555555554444, MASTERCARD",
            "2223003122003222, MASTERCARD",
            "378282246310005,  AMEX",
            "6011111111111117, UNKNOWN"
    })
    void detectsBrandFromIin(String pan, CardBrand expected) {
        assertThat(CardBrand.detect(pan)).isEqualTo(expected);
    }
}
