package com.laundry.mapper;

import com.laundry.dto.OrderSHistoryResponseDto;
import com.laundry.entity.OrderStatusHistory;

public class OrderSHistoryMapper {

    public static OrderSHistoryResponseDto toDto(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }

        return OrderSHistoryResponseDto.builder()
                .id(history.getId())
                .orderId(history.getOrder().getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedAt(history.getChangedAt())
                .changedBy(history.getChangedBy() != null ? history.getChangedBy() : null)
                .build();
    }
}
