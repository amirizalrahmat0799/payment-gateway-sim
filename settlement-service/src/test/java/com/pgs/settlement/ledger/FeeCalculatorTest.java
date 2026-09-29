package com.pgs.settlement.ledger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FeeCalculatorTest {

    @ParameterizedTest(name = "{1} bps of {0} = {2}")
    @CsvSource({
            "10000, 250, 250",   // 2.5% of 100.00 = 2.50
            "1001,  250, 25",    // 25.025 rounds down
            "1020,  250, 26",    // 25.5 rounds half-up
            "999,   0,   0",
            "1,     250, 0"
    })
    void appliesBasisPointsWithHalfUpRounding(long amount, int bps, long expectedFee) {
        assertThat(FeeCalculator.fee(amount, bps)).isEqualTo(expectedFee);
    }
}
