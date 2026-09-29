package com.pgs.settlement.batch;

import java.util.UUID;

/** Aggregated unsettled ledger activity for one merchant in one currency. */
public record MerchantTotals(UUID merchantId, String currency, long grossCaptured, long grossRefunded,
                             long fees, int entryCount) {
}
