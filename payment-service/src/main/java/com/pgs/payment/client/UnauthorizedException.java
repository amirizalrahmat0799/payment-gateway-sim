package com.pgs.payment.client;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Missing, invalid or inactive API key");
    }
}
