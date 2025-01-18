package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class InvalidCredentialsException extends ApiBaseException {
    public InvalidCredentialsException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.UNAUTHORIZED, reason, errors);
    }

    public InvalidCredentialsException(String reason) {
        this(reason, null);
    }
}
