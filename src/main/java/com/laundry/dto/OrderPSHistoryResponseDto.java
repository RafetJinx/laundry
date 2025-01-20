package com.laundry.dto;

import com.laundry.entity.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class OrderPSHistoryResponseDto {
    Long id;
    Long orderId;
    PaymentStatus oldPaymentStatus;
    PaymentStatus newPaymentStatus;
    LocalDateTime changedAt;
    Long changedBy;
}
