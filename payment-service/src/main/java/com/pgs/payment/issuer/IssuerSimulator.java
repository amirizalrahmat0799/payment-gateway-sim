package com.pgs.payment.issuer;

import com.pgs.payment.domain.CardSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Stands in for the card network / issuing bank. Outcomes are deterministic so they can be
 * demoed and tested with "magic" test cards:
 *
 * <table>
 *   <tr><th>Card ending</th><th>Outcome</th></tr>
 *   <tr><td>0002</td><td>declined: card_declined</td></tr>
 *   <tr><td>9995</td><td>declined: insufficient_funds</td></tr>
 *   <tr><td>any expired card</td><td>declined: expired_card</td></tr>
 *   <tr><td>amount above issuer.max-amount</td><td>declined: amount_limit_exceeded</td></tr>
 *   <tr><td>anything else</td><td>approved</td></tr>
 * </table>
 */
@Component
public class IssuerSimulator {

    private static final String AUTH_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final long maxAmount;

    public IssuerSimulator(@Value("${issuer.max-amount:5000000}") long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public AuthorizationDecision authorize(CardSnapshot card, long amount) {
        if (card.expired()) {
            return AuthorizationDecision.decline("expired_card");
        }
        if ("0002".equals(card.last4())) {
            return AuthorizationDecision.decline("card_declined");
        }
        if ("9995".equals(card.last4())) {
            return AuthorizationDecision.decline("insufficient_funds");
        }
        if (amount > maxAmount) {
            return AuthorizationDecision.decline("amount_limit_exceeded");
        }
        return AuthorizationDecision.approve(authCode());
    }

    private static String authCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(AUTH_CODE_CHARS.charAt(RANDOM.nextInt(AUTH_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
