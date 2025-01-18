package com.laundry.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public abstract class ApiBaseException extends RuntimeException {
    private final HttpStatus status;
    private final List<ErrorDetail> errors;

    public ApiBaseException(HttpStatus status, String reason, List<ErrorDetail> errors) {
        super(reason);
        this.status = status;
        this.errors = errors;
    }

}
