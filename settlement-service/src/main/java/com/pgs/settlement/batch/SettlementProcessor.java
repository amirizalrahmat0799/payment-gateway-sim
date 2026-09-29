package com.pgs.settlement.batch;

import org.springframework.batch.item.ItemProcessor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SettlementProcessor implements ItemProcessor<MerchantTotals, Settlement> {

    private final LocalDate settlementDate;

    public SettlementProcessor(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    @Override
    public Settlement process(MerchantTotals t) {
        long net = t.grossCaptured() - t.grossRefunded() - t.fees();
        return new Settlement(UUID.randomUUID(), t.merchantId(), t.currency(), settlementDate,
                t.grossCaptured(), t.grossRefunded(), t.fees(), net, t.entryCount(), Instant.now());
    }
}
