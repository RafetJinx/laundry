package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class InternalServerErrorException extends ApiBaseException {
    public InternalServerErrorException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason, errors);
    }

    public InternalServerErrorException(String reason) {
        this(reason, null);
    }
}
