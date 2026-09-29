package com.mizal.pgs.merchant.service;

public class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException() {
        super("Invalid or inactive API key");
    }
}
