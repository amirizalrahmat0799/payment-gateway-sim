package com.mizal.pgs.settlement.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Inserts the settlement and stamps every ledger entry it covers with the settlement id,
 * in the same chunk transaction, so an entry can never be settled twice.
 */
public class SettlementWriter implements ItemWriter<Settlement> {

    private final JdbcClient jdbc;
    private final Instant cutoff;

    public SettlementWriter(JdbcClient jdbc, Instant cutoff) {
        this.jdbc = jdbc;
        this.cutoff = cutoff;
    }

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        for (Settlement s : chunk) {
            jdbc.sql("""
                            INSERT INTO settlements (id, merchant_id, currency, settlement_date, gross_captured,
                                                     gross_refunded, fees, net_amount, entry_count, created_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """)
                    .params(s.id(), s.merchantId(), s.currency(), Date.valueOf(s.settlementDate()),
                            s.grossCaptured(), s.grossRefunded(), s.fees(), s.netAmount(), s.entryCount(),
                            Timestamp.from(s.createdAt()))
                    .update();

            int stamped = jdbc.sql("""
                            UPDATE ledger_entries SET settlement_id = ?
                            WHERE merchant_id = ? AND currency = ? AND settlement_id IS NULL AND occurred_at < ?
                            """)
                    .params(s.id(), s.merchantId(), s.currency(), Timestamp.from(cutoff))
                    .update();

            if (stamped != s.entryCount()) {
                // Rows changed between read and write (e.g. a second job run). Roll back the chunk.
                throw new IllegalStateException("Expected to settle " + s.entryCount()
                        + " ledger entries for merchant " + s.merchantId() + " but found " + stamped);
            }
        }
    }
}
