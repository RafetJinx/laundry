package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class NotFoundException extends ApiBaseException {
    public NotFoundException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.NOT_FOUND, reason, errors);
    }

    public NotFoundException(String reason) {
        this(reason, null);
    }
}
