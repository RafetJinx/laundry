package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Custom 403 exception if current user is not allowed to perform an action.
 */
public class AccessDeniedException extends ApiBaseException {

    public AccessDeniedException(String reason) {
        super(HttpStatus.FORBIDDEN, reason, null);
    }

    public AccessDeniedException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.FORBIDDEN, reason, errors);
    }
}
