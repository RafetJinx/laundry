package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OrderItemResponseDto {
    Long id;
    Long orderId;
    Long serviceId;
    BigDecimal priceAmount;
    Integer quantity;
    BigDecimal weight;
    String createdAt;
    String updatedAt;
}
