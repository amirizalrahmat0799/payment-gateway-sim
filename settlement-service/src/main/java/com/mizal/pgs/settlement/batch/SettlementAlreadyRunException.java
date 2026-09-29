package com.mizal.pgs.settlement.batch;

import java.time.LocalDate;

public class SettlementAlreadyRunException extends RuntimeException {

    public SettlementAlreadyRunException(LocalDate date) {
        super("Settlement for " + date + " has already completed");
    }
}
