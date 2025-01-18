package com.laundry.exception;

import java.util.List;

public interface ErrorTraceable {
    List<ErrorDetail> getErrors();
    String getTraceId();
}
