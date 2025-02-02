package com.laundry.service;

import java.time.LocalDateTime;

public interface OrderReportService {
    byte[] downloadOrderReport(LocalDateTime startDate, LocalDateTime endDate) throws Exception;

    void sendOrderReport(LocalDateTime startDate, LocalDateTime endDate) throws Exception;
}
