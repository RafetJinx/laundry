package com.laundry.exception;

import org.springframework.http.HttpStatus;
import java.util.List;

public class BadRequestException extends ApiBaseException {
    public BadRequestException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason, null);
    }
    public BadRequestException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.BAD_REQUEST, reason, errors);
    }
}
