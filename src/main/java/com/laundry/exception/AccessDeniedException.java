package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class AccessDeniedException extends ApiBaseException {

    public AccessDeniedException(String reason) {
        super(HttpStatus.FORBIDDEN, reason, null);
    }

    public AccessDeniedException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.FORBIDDEN, reason, errors);
    }
}
