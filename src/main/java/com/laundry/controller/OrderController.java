package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.security.JwtUtil;
import com.laundry.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @RequestBody OrderRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        OrderResponseDto created = orderService.createOrder(requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        OrderResponseDto orderDto = orderService.getOrderById(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Order found", orderDto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getAllOrders(
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        List<OrderResponseDto> orders = orderService.getAllOrders(currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("All orders fetched", orders));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrder(
            @PathVariable Long id,
            @RequestBody OrderRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        OrderResponseDto updated = orderService.updateOrder(id, requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> patchOrder(
            @PathVariable Long id,
            @RequestBody OrderRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        OrderResponseDto patched = orderService.patchOrder(id, requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Order patched successfully", patched));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrder(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        orderService.deleteOrder(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Order deleted", null));
    }

    @PatchMapping("/{id}/advance")
    public ResponseEntity<ApiResponse<OrderResponseDto>> advanceOrderStatus(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        OrderResponseDto advanced = orderService.advanceOrderStatus(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Order status advanced", advanced));
    }
}
