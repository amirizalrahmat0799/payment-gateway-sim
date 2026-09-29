package com.mizal.pgs.payment.client;

public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String service, Throwable cause) {
        super(service + " is unavailable", cause);
    }
}
