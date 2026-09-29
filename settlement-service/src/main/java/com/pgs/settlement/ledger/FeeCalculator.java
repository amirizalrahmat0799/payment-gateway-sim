package com.pgs.settlement.ledger;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Money is always integer minor units; percentages are applied with explicit rounding. */
public final class FeeCalculator {

    private static final BigDecimal BPS_DIVISOR = BigDecimal.valueOf(10_000);

    private FeeCalculator() {
    }

    /** @return fee in minor units, rounded half-up (e.g. 2.5% of 1001 = 25.025 → 25) */
    public static long fee(long amount, int feeBps) {
        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(feeBps))
                .divide(BPS_DIVISOR, 0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
