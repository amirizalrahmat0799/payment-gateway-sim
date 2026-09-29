package com.mizal.pgs.tokenization.vault;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException(String token) {
        super("Token " + token + " not found");
    }
}
