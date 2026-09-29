package com.pgs.payment.web.dto;

import jakarta.validation.constraints.Positive;

/** @param amount amount to capture; omit to capture the full authorized amount */
public record CaptureRequest(@Positive Long amount) {
}
