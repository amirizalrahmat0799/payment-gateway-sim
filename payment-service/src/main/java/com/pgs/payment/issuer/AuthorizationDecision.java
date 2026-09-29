package com.pgs.payment.issuer;

public record AuthorizationDecision(boolean approved, String authCode, String declineReason) {

    public static AuthorizationDecision approve(String authCode) {
        return new AuthorizationDecision(true, authCode, null);
    }

    public static AuthorizationDecision decline(String reason) {
        return new AuthorizationDecision(false, null, reason);
    }
}
