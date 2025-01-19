package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class BadCredentialsException extends ApiBaseException {

    public BadCredentialsException(String reason) {
        super(HttpStatus.UNAUTHORIZED, reason, null);
    }

    public BadCredentialsException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.UNAUTHORIZED, reason, errors);
    }
}
