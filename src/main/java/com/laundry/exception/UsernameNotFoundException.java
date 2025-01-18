package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Specialized 404 for "username not found" scenarios,
 * now extends ApiBaseException, not ResponseStatusException.
 */
public class UsernameNotFoundException extends ApiBaseException {

    public UsernameNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason, null);
    }

    public UsernameNotFoundException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.NOT_FOUND, reason, errors);
    }
}
