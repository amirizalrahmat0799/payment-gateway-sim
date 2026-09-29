package com.mizal.pgs.payment.client;

import com.mizal.pgs.payment.domain.CardSnapshot;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class TokenizationClient {

    private final RestClient restClient;

    public TokenizationClient(@Qualifier("tokenizationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    record TokenResponse(String token, String brand, String last4, boolean expired) {
    }

    public CardSnapshot lookup(String cardToken) {
        try {
            TokenResponse response = restClient.get()
                    .uri("/internal/tokens/{token}", cardToken)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (request, res) -> {
                        throw new UnknownCardTokenException(cardToken);
                    })
                    .body(TokenResponse.class);
            return new CardSnapshot(response.token(), response.brand(), response.last4(), response.expired());
        } catch (UnknownCardTokenException e) {
            throw e;
        } catch (RestClientException e) {
            throw new DownstreamUnavailableException("tokenization-service", e);
        }
    }
}
