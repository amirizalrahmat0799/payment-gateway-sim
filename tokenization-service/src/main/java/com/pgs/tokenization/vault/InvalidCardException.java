package com.pgs.tokenization.vault;

public class InvalidCardException extends RuntimeException {

    public InvalidCardException(String message) {
        super(message);
    }
}
