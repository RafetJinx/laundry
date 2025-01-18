package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class InvalidEmailException extends ApiBaseException {
    public InvalidEmailException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.BAD_REQUEST, reason, errors);
    }

    public InvalidEmailException(String reason) {
        this(reason, null);
    }
}
