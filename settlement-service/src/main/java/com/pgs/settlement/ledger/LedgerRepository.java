package com.pgs.settlement.ledger;

import com.pgs.common.event.PaymentEvent;
import com.pgs.common.event.PaymentEventType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class LedgerRepository {

    private final JdbcClient jdbc;

    public LedgerRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public record LedgerEntry(UUID id, UUID paymentId, UUID merchantId, String entryType, long amount,
                              String currency, long fee, Instant occurredAt, UUID settlementId) {
    }

    /**
     * Records the event exactly once, even if Kafka delivers it more than once.
     *
     * @return true if inserted, false if it was a duplicate
     */
    public boolean record(PaymentEvent event) {
        boolean capture = event.type() == PaymentEventType.CAPTURED;
        // Refunds do not return the processing fee (common acquirer behaviour).
        long fee = capture ? FeeCalculator.fee(event.amount(), event.feeBps()) : 0;
        int rows = jdbc.sql("""
                        INSERT INTO ledger_entries
                            (id, payment_id, merchant_id, entry_type, amount, currency, fee, occurred_at, created_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())
                        ON CONFLICT (id) DO NOTHING
                        """)
                .params(event.eventId(), event.paymentId(), event.merchantId(), capture ? "CAPTURE" : "REFUND",
                        event.amount(), event.currency(), fee, Timestamp.from(event.occurredAt()))
                .update();
        return rows == 1;
    }

    public List<LedgerEntry> recentForMerchant(UUID merchantId, int limit) {
        return jdbc.sql("""
                        SELECT id, payment_id, merchant_id, entry_type, amount, currency, fee, occurred_at, settlement_id
                        FROM ledger_entries WHERE merchant_id = ?
                        ORDER BY occurred_at DESC LIMIT ?
                        """)
                .params(merchantId, limit)
                .query((rs, i) -> new LedgerEntry(
                        rs.getObject("id", UUID.class),
                        rs.getObject("payment_id", UUID.class),
                        rs.getObject("merchant_id", UUID.class),
                        rs.getString("entry_type"),
                        rs.getLong("amount"),
                        rs.getString("currency"),
                        rs.getLong("fee"),
                        rs.getTimestamp("occurred_at").toInstant(),
                        rs.getObject("settlement_id", UUID.class)))
                .list();
    }
}
