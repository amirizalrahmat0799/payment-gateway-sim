package com.mizal.pgs.payment.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
public class MerchantClient {

    private final RestClient restClient;

    public MerchantClient(@Qualifier("merchantRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public record MerchantContext(UUID merchantId, String name, int feeBps) {
    }

    public MerchantContext authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new UnauthorizedException();
        }
        try {
            return restClient.post()
                    .uri("/internal/merchants/authenticate")
                    .header("X-Api-Key", apiKey)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.UNAUTHORIZED.value(), (request, response) -> {
                        throw new UnauthorizedException();
                    })
                    .body(MerchantContext.class);
        } catch (UnauthorizedException e) {
            throw e;
        } catch (RestClientException e) {
            throw new DownstreamUnavailableException("merchant-service", e);
        }
    }
}
