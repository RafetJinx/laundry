package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Specialized 500 error for deletion failures,
 * now extends ApiBaseException, not ResponseStatusException.
 */
public class DeletionException extends ApiBaseException {

    public DeletionException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    public DeletionException(String message, List<ErrorDetail> errors) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, errors);
    }
}
