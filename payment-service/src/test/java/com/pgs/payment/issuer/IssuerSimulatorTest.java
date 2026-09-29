package com.pgs.payment.issuer;

import com.pgs.payment.domain.CardSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class IssuerSimulatorTest {

    private final IssuerSimulator issuer = new IssuerSimulator(5_000_000);

    @Test
    void approvesNormalCardWithAuthCode() {
        AuthorizationDecision decision = issuer.authorize(card("4242", false), 10_000);

        assertThat(decision.approved()).isTrue();
        assertThat(decision.authCode()).hasSize(6);
    }

    @ParameterizedTest
    @CsvSource({
            "0002, false, 1000,    card_declined",
            "9995, false, 1000,    insufficient_funds",
            "4242, true,  1000,    expired_card",
            "4242, false, 5000001, amount_limit_exceeded"
    })
    void declinesMagicCardsAndLimits(String last4, boolean expired, long amount, String reason) {
        AuthorizationDecision decision = issuer.authorize(card(last4, expired), amount);

        assertThat(decision.approved()).isFalse();
        assertThat(decision.declineReason()).isEqualTo(reason);
    }

    private static CardSnapshot card(String last4, boolean expired) {
        return new CardSnapshot("tok_test", "VISA", last4, expired);
    }
}
