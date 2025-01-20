package com.laundry.controller;

import com.laundry.dto.ApiResponse;
import com.laundry.dto.OrderPSHistoryResponseDto;
import com.laundry.entity.PaymentStatus;
import com.laundry.service.OrderPSHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/orders/payment-status-history")
public class AdminOrderPSHistoryController {

    @Autowired
    private OrderPSHistoryService orderPSHistoryService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderPSHistoryResponseDto>> getOrderPSHistoryById(@PathVariable Long id) {
        OrderPSHistoryResponseDto history = orderPSHistoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("History found", history));
    }

    @GetMapping("/by-order")
    public ResponseEntity<ApiResponse<Page<OrderPSHistoryResponseDto>>> getOrderPSHistoryByOrderId(
            @RequestParam Long orderId,
            Pageable pageable
    ) {
        Page<OrderPSHistoryResponseDto> historyPage = orderPSHistoryService.findByOrderId(orderId, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by order ID", historyPage));
    }

    @GetMapping("/by-old-status")
    public ResponseEntity<ApiResponse<Page<OrderPSHistoryResponseDto>>> getOrderPSHistoryByOldStatus(
            @RequestParam PaymentStatus oldPaymentStatus,
            Pageable pageable
    ) {
        Page<OrderPSHistoryResponseDto> historyPage = orderPSHistoryService.findByOldPaymentStatus(oldPaymentStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by old payment status", historyPage));
    }

    @GetMapping("/by-new-status")
    public ResponseEntity<ApiResponse<Page<OrderPSHistoryResponseDto>>> getOrderPSHistoryByNewStatus(
            @RequestParam PaymentStatus newPaymentStatus,
            Pageable pageable
    ) {
        Page<OrderPSHistoryResponseDto> historyPage = orderPSHistoryService.findByNewPaymentStatus(newPaymentStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by new payment status", historyPage));
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<ApiResponse<Page<OrderPSHistoryResponseDto>>> getOrderPSHistoryByDateRange(
            @RequestParam("after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtAfter,
            @RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtBefore,
            Pageable pageable
    ) {
        Page<OrderPSHistoryResponseDto> historyPage =
                orderPSHistoryService.findByChangedAtBetween(changedAtAfter, changedAtBefore, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by date range", historyPage));
    }

    @GetMapping("/by-changed-by")
    public ResponseEntity<ApiResponse<Page<OrderPSHistoryResponseDto>>> getOrderPSHistoryByChangedBy(
            @RequestParam Long changedBy,
            Pageable pageable
    ) {
        Page<OrderPSHistoryResponseDto> historyPage = orderPSHistoryService.findByChangedBy(changedBy, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by changedBy", historyPage));
    }

}
