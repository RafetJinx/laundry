package com.laundry.dto;

import com.laundry.entity.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OrderSHistoryResponseDto {
    Long id;
    Long orderId;
    OrderStatus oldStatus;
    OrderStatus newStatus;
    LocalDateTime changedAt;
    Long changedBy;
}
