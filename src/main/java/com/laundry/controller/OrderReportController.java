package com.laundry.controller;

import com.laundry.dto.ApiResponse;
import com.laundry.service.OrderReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/order-report")
@Slf4j
public class OrderReportController {

    private final OrderReportService orderReportService;

    public OrderReportController(OrderReportService orderReportService) {
        this.orderReportService = orderReportService;
    }

    @GetMapping("/download")
    public ResponseEntity<ApiResponse<byte[]>> downloadReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            byte[] excelBytes = orderReportService.downloadOrderReport(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Order report generated", excelBytes));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error generating report: " + e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<?>> sendReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            orderReportService.sendOrderReport(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Order report sent successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error sending report: " + e.getMessage()));
        }
    }
}
