package com.mizal.pgs.settlement.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Daily settlement job: read → aggregate unsettled ledger entries per merchant/currency,
 * process → compute net payout, write → persist settlement and mark entries settled.
 * <p>
 * Job parameter {@code settlementDate} (yyyy-MM-dd) identifies the job instance, so Spring Batch
 * refuses to run the same date twice once it has completed.
 */
@Configuration
public class SettlementJobConfig {

    public static final String JOB_NAME = "dailySettlementJob";
    public static final String DATE_PARAM = "settlementDate";

    private static final String TOTALS_SQL = """
            SELECT merchant_id, currency,
                   SUM(CASE WHEN entry_type = 'CAPTURE' THEN amount ELSE 0 END) AS gross_captured,
                   SUM(CASE WHEN entry_type = 'REFUND'  THEN amount ELSE 0 END) AS gross_refunded,
                   SUM(fee)  AS fees,
                   COUNT(*)  AS entry_count
            FROM ledger_entries
            WHERE settlement_id IS NULL AND occurred_at < ?
            GROUP BY merchant_id, currency
            ORDER BY merchant_id, currency
            """;

    /** Everything that happened before midnight UTC at the end of the settlement date. */
    static Instant cutoff(String settlementDate) {
        return LocalDate.parse(settlementDate).plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    @Bean
    Job dailySettlementJob(JobRepository jobRepository, Step settleMerchantsStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(settleMerchantsStep)
                .build();
    }

    @Bean
    Step settleMerchantsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             JdbcCursorItemReader<MerchantTotals> merchantTotalsReader,
                             SettlementProcessor settlementProcessor,
                             SettlementWriter settlementWriter,
                             @Value("${settlement.chunk-size:50}") int chunkSize) {
        return new StepBuilder("settleMerchants", jobRepository)
                .<MerchantTotals, Settlement>chunk(chunkSize, transactionManager)
                .reader(merchantTotalsReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .build();
    }

    @Bean
    @StepScope
    JdbcCursorItemReader<MerchantTotals> merchantTotalsReader(
            DataSource dataSource, @Value("#{jobParameters['" + DATE_PARAM + "']}") String settlementDate) {
        Timestamp cutoff = Timestamp.from(cutoff(settlementDate));
        return new JdbcCursorItemReaderBuilder<MerchantTotals>()
                .name("merchantTotalsReader")
                .dataSource(dataSource)
                .sql(TOTALS_SQL)
                .preparedStatementSetter(ps -> ps.setTimestamp(1, cutoff))
                .rowMapper((rs, i) -> new MerchantTotals(
                        rs.getObject("merchant_id", UUID.class),
                        rs.getString("currency"),
                        rs.getLong("gross_captured"),
                        rs.getLong("gross_refunded"),
                        rs.getLong("fees"),
                        rs.getInt("entry_count")))
                .build();
    }

    @Bean
    @StepScope
    SettlementProcessor settlementProcessor(@Value("#{jobParameters['" + DATE_PARAM + "']}") String settlementDate) {
        return new SettlementProcessor(LocalDate.parse(settlementDate));
    }

    @Bean
    @StepScope
    SettlementWriter settlementWriter(JdbcClient jdbcClient,
                                      @Value("#{jobParameters['" + DATE_PARAM + "']}") String settlementDate) {
        return new SettlementWriter(jdbcClient, cutoff(settlementDate));
    }
}
