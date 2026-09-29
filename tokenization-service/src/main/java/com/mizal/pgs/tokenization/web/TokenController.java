package com.mizal.pgs.tokenization.web;

import com.mizal.pgs.tokenization.vault.CardToken;
import com.mizal.pgs.tokenization.vault.TokenizationService;
import com.mizal.pgs.tokenization.web.dto.TokenResponse;
import com.mizal.pgs.tokenization.web.dto.TokenizeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    private final TokenizationService tokenizationService;

    public TokenController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    /**
     * Public endpoint, typically called straight from the checkout page so the raw card number
     * never touches the merchant's servers (this is what keeps merchants out of PCI DSS scope).
     */
    @PostMapping("/api/v1/tokens")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse tokenize(@Valid @RequestBody TokenizeRequest request) {
        CardToken token = tokenizationService.tokenize(request);
        return TokenResponse.from(token, false);
    }

    /** Internal: payment-service looks up card metadata. The PAN is never returned. */
    @GetMapping("/internal/tokens/{token}")
    public TokenResponse lookup(@PathVariable String token) {
        CardToken cardToken = tokenizationService.get(token);
        return TokenResponse.from(cardToken, tokenizationService.isExpired(cardToken));
    }
}
