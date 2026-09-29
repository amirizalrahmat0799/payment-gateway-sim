package com.pgs.merchant.service;

public class MerchantAlreadyExistsException extends RuntimeException {

    public MerchantAlreadyExistsException(String email) {
        super("A merchant with email " + email + " already exists");
    }
}
