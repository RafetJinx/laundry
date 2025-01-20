package com.laundry.mapper;

import com.laundry.dto.OrderSHistoryResponseDto;
import com.laundry.entity.OrderStatusHistory;

public class OrderSHistoryMapper {

    public static OrderSHistoryResponseDto toDto(OrderStatusHistory entity) {
        if (entity == null) {
            return null;
        }

        return OrderSHistoryResponseDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder().getId())
                .oldStatus(entity.getOldStatus())
                .newStatus(entity.getNewStatus())
                .changedAt(entity.getChangedAt())
                .changedBy(entity.getChangedBy() != null ? entity.getChangedBy() : null)
                .build();
    }
}
