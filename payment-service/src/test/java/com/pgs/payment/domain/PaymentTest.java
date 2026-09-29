package com.pgs.payment.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final CardSnapshot CARD = new CardSnapshot("tok_abc", "VISA", "4242", false);

    private static Payment authorized(long amount) {
        return Payment.authorized(UUID.randomUUID(), amount, "MYR", CARD, 250, "test", "AB12CD");
    }

    @Test
    void fullCaptureWhenNoAmountGiven() {
        Payment p = authorized(10_000);

        assertThat(p.capture(null)).isEqualTo(10_000);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(p.refundableAmount()).isEqualTo(10_000);
    }

    @Test
    void partialCaptureReleasesRemainder() {
        Payment p = authorized(10_000);

        p.capture(7_500L);

        assertThat(p.getCapturedAmount()).isEqualTo(7_500);
        assertThat(p.refundableAmount()).isEqualTo(7_500);
    }

    @Test
    void cannotCaptureMoreThanAuthorized() {
        Payment p = authorized(10_000);

        assertThatThrownBy(() -> p.capture(10_001L)).isInstanceOf(InvalidPaymentOperationException.class);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }

    @Test
    void cannotCaptureTwice() {
        Payment p = authorized(10_000);
        p.capture(null);

        assertThatThrownBy(() -> p.capture(null))
                .isInstanceOf(InvalidPaymentOperationException.class)
                .hasMessageContaining("CAPTURED");
    }

    @Test
    void partialThenFullRefund() {
        Payment p = authorized(10_000);
        p.capture(null);

        p.refund(3_000);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        assertThat(p.refundableAmount()).isEqualTo(7_000);

        p.refund(7_000);
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(p.refundableAmount()).isZero();
    }

    @Test
    void cannotRefundMoreThanCaptured() {
        Payment p = authorized(10_000);
        p.capture(5_000L);

        assertThatThrownBy(() -> p.refund(5_001)).isInstanceOf(InvalidPaymentOperationException.class);
    }

    @Test
    void cannotRefundUncapturedPayment() {
        Payment p = authorized(10_000);

        assertThatThrownBy(() -> p.refund(100)).isInstanceOf(InvalidPaymentOperationException.class);
    }

    @Test
    void voidOnlyFromAuthorized() {
        Payment p = authorized(10_000);
        p.voidAuthorization();
        assertThat(p.getStatus()).isEqualTo(PaymentStatus.VOIDED);

        assertThatThrownBy(p::voidAuthorization).isInstanceOf(InvalidPaymentOperationException.class);
        assertThatThrownBy(() -> p.capture(null)).isInstanceOf(InvalidPaymentOperationException.class);
    }

    @Test
    void declinedPaymentIsTerminal() {
        Payment p = Payment.declined(UUID.randomUUID(), 10_000, "MYR", CARD, 250, null, "card_declined");

        assertThat(p.getDeclineReason()).isEqualTo("card_declined");
        assertThatThrownBy(() -> p.capture(null)).isInstanceOf(InvalidPaymentOperationException.class);
        assertThatThrownBy(p::voidAuthorization).isInstanceOf(InvalidPaymentOperationException.class);
    }
}
