package com.mizal.pgs.payment.domain;

/** Non-sensitive card details copied onto the payment at authorization time. */
public record CardSnapshot(String token, String brand, String last4, boolean expired) {
}
