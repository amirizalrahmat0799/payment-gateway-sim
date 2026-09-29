package com.mizal.pgs.merchant.web;

import com.mizal.pgs.merchant.service.InvalidApiKeyException;
import com.mizal.pgs.merchant.service.MerchantAlreadyExistsException;
import com.mizal.pgs.merchant.service.MerchantNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Maps domain exceptions to RFC 9457 problem details. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MerchantNotFoundException.class)
    ProblemDetail notFound(MerchantNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    ProblemDetail conflict(MerchantAlreadyExistsException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    ProblemDetail unauthorized(InvalidApiKeyException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}
