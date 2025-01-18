package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * CHANGED: In Spring, custom exceptions typically extend RuntimeException
 * to allow transaction rollbacks as needed, and to avoid mandatory try/catch.
 */
public abstract class ApiBaseException extends RuntimeException {
    private final HttpStatus status;
    private final List<ErrorDetail> errors;

    public ApiBaseException(HttpStatus status, String reason, List<ErrorDetail> errors) {
        super(reason);
        this.status = status;
        this.errors = errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }
}
