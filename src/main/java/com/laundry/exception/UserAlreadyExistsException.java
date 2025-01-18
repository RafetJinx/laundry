package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class UserAlreadyExistsException extends ApiBaseException {
    public UserAlreadyExistsException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.CONFLICT, reason, errors);
    }

    public UserAlreadyExistsException(String reason) {
        this(reason, null);
    }
}
