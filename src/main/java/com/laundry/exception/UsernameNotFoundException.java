package com.laundry.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class UsernameNotFoundException extends ApiBaseException {

    public UsernameNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason, null);
    }

    public UsernameNotFoundException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.NOT_FOUND, reason, errors);
    }
}
