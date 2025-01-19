package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class IllegalArgumentException extends ApiBaseException {

    public IllegalArgumentException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason, null);
    }

    public IllegalArgumentException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.BAD_REQUEST, reason, errors);
    }
}
