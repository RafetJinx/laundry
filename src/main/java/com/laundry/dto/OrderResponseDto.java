package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class OrderResponseDto {
    Long id;
    Long userId;
    Long productId;
    String referenceNo;
    BigDecimal totalAmount;
    String currencyCode;
    String paymentStatus;
    String orderStatus;
    String createdAt;
    String updatedAt;

    List<OrderItemResponseDto> orderItems;
}