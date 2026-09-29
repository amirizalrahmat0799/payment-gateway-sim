package com.mizal.pgs.payment.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients")
public record ClientProperties(Endpoint merchant, Endpoint tokenization) {

    public record Endpoint(String baseUrl) {
    }
}
