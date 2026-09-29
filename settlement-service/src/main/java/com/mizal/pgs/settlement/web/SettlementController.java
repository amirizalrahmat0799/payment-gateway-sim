package com.mizal.pgs.settlement.web;

import com.mizal.pgs.settlement.batch.Settlement;
import com.mizal.pgs.settlement.batch.SettlementRunner;
import com.mizal.pgs.settlement.ledger.LedgerRepository;
import com.mizal.pgs.settlement.ledger.LedgerRepository.LedgerEntry;
import org.springframework.batch.core.JobExecution;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class SettlementController {

    private final SettlementRunner runner;
    private final LedgerRepository ledger;
    private final JdbcClient jdbc;

    public SettlementController(SettlementRunner runner, LedgerRepository ledger, JdbcClient jdbc) {
        this.runner = runner;
        this.ledger = ledger;
        this.jdbc = jdbc;
    }

    public record RunResponse(Long jobExecutionId, LocalDate settlementDate, String status, String exitDescription) {
    }

    /** Manually trigger settlement for a date (handy for demos: pass today's date). */
    @PostMapping("/settlements/run")
    public RunResponse run(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        JobExecution execution = runner.run(date);
        return new RunResponse(execution.getId(), date, execution.getStatus().name(),
                execution.getExitStatus().getExitDescription());
    }

    @GetMapping("/settlements")
    public List<Settlement> settlements(@RequestParam UUID merchantId) {
        return jdbc.sql("""
                        SELECT id, merchant_id, currency, settlement_date, gross_captured, gross_refunded,
                               fees, net_amount, entry_count, created_at
                        FROM settlements WHERE merchant_id = ?
                        ORDER BY settlement_date DESC, created_at DESC
                        """)
                .param(merchantId)
                .query((rs, i) -> new Settlement(
                        rs.getObject("id", UUID.class),
                        rs.getObject("merchant_id", UUID.class),
                        rs.getString("currency"),
                        rs.getDate("settlement_date").toLocalDate(),
                        rs.getLong("gross_captured"),
                        rs.getLong("gross_refunded"),
                        rs.getLong("fees"),
                        rs.getLong("net_amount"),
                        rs.getInt("entry_count"),
                        rs.getTimestamp("created_at").toInstant()))
                .list();
    }

    @GetMapping("/ledger")
    public List<LedgerEntry> ledger(@RequestParam UUID merchantId,
                                    @RequestParam(defaultValue = "100") int limit) {
        return ledger.recentForMerchant(merchantId, Math.min(limit, 500));
    }
}
