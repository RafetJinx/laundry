package com.laundry.exception;

import lombok.Getter;

@Getter
public class ErrorDetail {
    private final String code;
    private final String message;
    private final String field;

    public ErrorDetail(String code, String message, String field) {
        this.code = code;
        this.message = message;
        this.field = field;
    }

}
