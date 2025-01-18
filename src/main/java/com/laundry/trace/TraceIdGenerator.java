package com.laundry.trace;

import java.util.UUID;

public class TraceIdGenerator {
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
