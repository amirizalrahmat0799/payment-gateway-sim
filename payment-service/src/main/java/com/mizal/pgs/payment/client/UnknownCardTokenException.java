package com.mizal.pgs.payment.client;

public class UnknownCardTokenException extends RuntimeException {

    public UnknownCardTokenException(String token) {
        super("Unknown card token " + token);
    }
}
