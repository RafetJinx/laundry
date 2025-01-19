package com.laundry.exception;

import org.springframework.http.HttpStatus;
import java.util.List;

public class RestClientException extends ApiBaseException {

    public RestClientException(String reason) {
        super(HttpStatus.BAD_GATEWAY, reason, null);
    }

    public RestClientException(String reason, List<ErrorDetail> errors) {
        super(HttpStatus.BAD_GATEWAY, reason, errors);
    }
}
