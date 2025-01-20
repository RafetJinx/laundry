package com.laundry.mapper;

import com.laundry.dto.OrderPSHistoryResponseDto;
import com.laundry.entity.OrderPaymentStatusHistory;

public class OrderPSHistoryMapper {

    public static OrderPSHistoryResponseDto toDto(OrderPaymentStatusHistory history) {
        if (history == null) {
            return null;
        }

        return OrderPSHistoryResponseDto.builder()
                .id(history.getId())
                .orderId(history.getOrder().getId())
                .oldPaymentStatus(history.getOldPaymentStatus())
                .newPaymentStatus(history.getNewPaymentStatus())
                .changedAt(history.getChangedAt())
                .changedBy(history.getChangedBy() != null ? history.getChangedBy() : null)
                .build();
    }
}
