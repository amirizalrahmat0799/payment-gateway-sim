package com.pgs.settlement.batch;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SettlementProcessorTest {

    private final LocalDate date = LocalDate.of(2026, 9, 29);
    private final SettlementProcessor processor = new SettlementProcessor(date);

    @Test
    void netIsCapturedMinusRefundsMinusFees() {
        UUID merchant = UUID.randomUUID();

        Settlement s = processor.process(new MerchantTotals(merchant, "MYR", 100_000, 20_000, 2_500, 7));

        assertThat(s.merchantId()).isEqualTo(merchant);
        assertThat(s.settlementDate()).isEqualTo(date);
        assertThat(s.netAmount()).isEqualTo(77_500);
        assertThat(s.entryCount()).isEqualTo(7);
    }

    @Test
    void netCanGoNegativeWhenRefundsExceedSales() {
        Settlement s = processor.process(new MerchantTotals(UUID.randomUUID(), "MYR", 1_000, 5_000, 25, 2));

        assertThat(s.netAmount()).isEqualTo(-4_025);
    }

    @Test
    void cutoffIsMidnightUtcAfterSettlementDate() {
        assertThat(SettlementJobConfig.cutoff("2026-09-29")).isEqualTo(Instant.parse("2026-09-30T00:00:00Z"));
    }
}
