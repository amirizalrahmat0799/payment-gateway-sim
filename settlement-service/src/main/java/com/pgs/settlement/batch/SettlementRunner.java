package com.pgs.settlement.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class SettlementRunner {

    private static final Logger log = LoggerFactory.getLogger(SettlementRunner.class);

    private final JobLauncher jobLauncher;
    private final Job dailySettlementJob;
    private final Clock clock = Clock.system(ZoneOffset.UTC);

    public SettlementRunner(JobLauncher jobLauncher, Job dailySettlementJob) {
        this.jobLauncher = jobLauncher;
        this.dailySettlementJob = dailySettlementJob;
    }

    @Scheduled(cron = "${settlement.cron:0 5 0 * * *}", zone = "UTC")
    public void settleYesterday() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);
        try {
            run(yesterday);
        } catch (SettlementAlreadyRunException e) {
            log.info("Settlement for {} already completed", yesterday);
        }
    }

    public JobExecution run(LocalDate settlementDate) {
        JobParameters params = new JobParametersBuilder()
                .addString(SettlementJobConfig.DATE_PARAM, settlementDate.toString())
                .toJobParameters();
        try {
            JobExecution execution = jobLauncher.run(dailySettlementJob, params);
            log.info("Settlement for {} finished with status {}", settlementDate, execution.getStatus());
            return execution;
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new SettlementAlreadyRunException(settlementDate);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobParametersInvalidException e) {
            throw new IllegalStateException("Could not launch settlement for " + settlementDate, e);
        }
    }
}
