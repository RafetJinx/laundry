package com.laundry.exception;

import org.springframework.http.HttpStatus;
import java.util.List;

public class NoSuchElementException extends ApiBaseException {

    public NoSuchElementException(String reason) {
        super(HttpStatus.NOT_FOUND, reason, null);
    }

    public NoSuchElementException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.NOT_FOUND, reason, errors);
    }
}
