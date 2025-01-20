package com.laundry.mapper;

import com.laundry.dto.OrderPSHistoryResponseDto;
import com.laundry.entity.OrderPaymentStatusHistory;

public class OrderPSHistoryMapper {

    public static OrderPSHistoryResponseDto toDto(OrderPaymentStatusHistory entity) {
        if (entity == null) {
            return null;
        }

        return OrderPSHistoryResponseDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .oldPaymentStatus(entity.getOldPaymentStatus())
                .newPaymentStatus(entity.getNewPaymentStatus())
                .changedAt(entity.getChangedAt())
                .changedBy(entity.getChangedBy() != null ? entity.getChangedBy() : null)
                .build();
    }
}
