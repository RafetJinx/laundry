package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.security.JwtUtil;
import com.laundry.service.OrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders/{orderId}/items")
public class OrderItemController {

    @Autowired
    private OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderItemResponseDto>> createOrderItem(
            @PathVariable Long orderId,
            @RequestBody OrderItemRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        OrderItemResponseDto created = orderItemService.createOrderItem(
                orderId,
                requestDto,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(ApiResponse.success("Order item created successfully", created));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<OrderItemResponseDto>> getOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        OrderItemResponseDto dto = orderItemService.getOrderItem(
                itemId,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(ApiResponse.success("Order item found", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderItemResponseDto>>> getItemsByOrder(
            @PathVariable Long orderId,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        List<OrderItemResponseDto> items = orderItemService.getItemsByOrder(
                orderId,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(ApiResponse.success("Order items for order " + orderId, items));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<OrderItemResponseDto>> updateOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody OrderItemRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        OrderItemResponseDto updated = orderItemService.updateOrderItem(
                itemId,
                requestDto,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(ApiResponse.success("Order item updated successfully", updated));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<?>> deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

        orderItemService.deleteOrderItem(
                itemId,
                currentUserId,
                currentUserRole
        );
        return ResponseEntity.ok(ApiResponse.success("Order item deleted", null));
    }
}
