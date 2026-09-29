package com.mizal.pgs.payment.web.dto;

import jakarta.validation.constraints.Positive;

public record RefundRequest(@Positive long amount) {
}
