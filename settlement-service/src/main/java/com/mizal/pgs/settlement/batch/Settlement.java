package com.mizal.pgs.settlement.batch;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A payout to one merchant: net = captured - refunded - fees (can be negative if refunds dominate). */
public record Settlement(UUID id, UUID merchantId, String currency, LocalDate settlementDate,
                         long grossCaptured, long grossRefunded, long fees, long netAmount,
                         int entryCount, Instant createdAt) {
}
