package com.laundry.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorResponse {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final List<ErrorDetail> errors;
    private final String traceId;

    public ErrorResponse(String timestamp,
                         int status,
                         String error,
                         String message,
                         List<ErrorDetail> errors,
                         String traceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
        this.traceId = traceId;
    }

}
