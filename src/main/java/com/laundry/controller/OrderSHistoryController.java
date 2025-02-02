package com.laundry.controller;

import com.laundry.dto.ApiResponse;
import com.laundry.dto.OrderSHistoryResponseDto;
import com.laundry.entity.OrderStatus;
import com.laundry.service.OrderSHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders/status-history")
public class OrderSHistoryController {

    private final OrderSHistoryService orderSHistoryService;

    public OrderSHistoryController(OrderSHistoryService orderSHistoryService) {
        this.orderSHistoryService = orderSHistoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderSHistoryResponseDto>> getOrderSHistoryById(@PathVariable Long id) {
        OrderSHistoryResponseDto history = orderSHistoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("History found", history));
    }

    @GetMapping("/by-order")
    public ResponseEntity<ApiResponse<Page<OrderSHistoryResponseDto>>> getOrderSHistoryByOrderId(
            @RequestParam Long orderId,
            Pageable pageable
    ) {
        Page<OrderSHistoryResponseDto> historyPage = orderSHistoryService.findByOrderId(orderId, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by order ID", historyPage));
    }

    @GetMapping("/by-old-status")
    public ResponseEntity<ApiResponse<Page<OrderSHistoryResponseDto>>> getOrderSHistoryByOldStatus(
            @RequestParam OrderStatus oldStatus,
            Pageable pageable
    ) {
        Page<OrderSHistoryResponseDto> historyPage = orderSHistoryService.findByOldStatus(oldStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by old status", historyPage));
    }

    @GetMapping("/by-new-status")
    public ResponseEntity<ApiResponse<Page<OrderSHistoryResponseDto>>> getOrderSHistoryByNewStatus(
            @RequestParam OrderStatus newStatus,
            Pageable pageable
    ) {
        Page<OrderSHistoryResponseDto> historyPage = orderSHistoryService.findByNewStatus(newStatus, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by new status", historyPage));
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<ApiResponse<Page<OrderSHistoryResponseDto>>> getOrderSHistoryByDateRange(
            @RequestParam("after") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtAfter,
            @RequestParam("before") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime changedAtBefore,
            Pageable pageable
    ) {
        Page<OrderSHistoryResponseDto> historyPage = orderSHistoryService
                .findByChangedAtBetween(changedAtAfter, changedAtBefore, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by date range", historyPage));
    }

    @GetMapping("/by-changed-by")
    public ResponseEntity<ApiResponse<Page<OrderSHistoryResponseDto>>> getOrderSHistoryByChangedBy(
            @RequestParam Long changedBy,
            Pageable pageable
    ) {
        Page<OrderSHistoryResponseDto> historyPage = orderSHistoryService.findByChangedBy(changedBy, pageable);
        return ResponseEntity.ok(ApiResponse.success("History found by changedBy", historyPage));
    }
}
