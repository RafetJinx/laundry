package com.laundry.mapper;

import com.laundry.dto.*;
import com.laundry.entity.*;
import com.laundry.util.OrderUtil;

import java.util.List;

import static com.laundry.util.DateTimeUtil.formatLocalDateTime;

public class OrderMapper {

    public static Order toEntity(OrderRequestDto dto,
                                 User user,
                                 List<Service> foundServices) {
        if (dto == null) return null;

        Order order = new Order();
        order.setUser(user);
        order.setCurrencyCode(dto.getCurrencyCode());
        order.setPaymentStatus(OrderUtil.parsePaymentStatus(dto.getPaymentStatus()));
        order.setStatus(OrderUtil.parseOrderStatus(dto.getOrderStatus()));

        if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            List<OrderItem> items = OrderUtil.buildOrderItems(order, dto, foundServices);
            order.setOrderItems(items);
        }
        return order;
    }

    public static OrderResponseDto toResponseDto(Order entity) {
        if (entity == null) {
            return null;
        }

        List<OrderItemResponseDto> itemDtos = null;
        if (entity.getOrderItems() != null && !entity.getOrderItems().isEmpty()) {
            itemDtos = entity.getOrderItems().stream()
                    .map(OrderItemMapper::toResponseDto)
                    .toList();
        }

        return OrderResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .totalAmount(entity.getTotalAmount())
                .currencyCode(entity.getCurrencyCode())
                .paymentStatus(entity.getPaymentStatus() != null
                        ? entity.getPaymentStatus().name()
                        : null)
                .orderStatus(entity.getStatus() != null
                        ? entity.getStatus().name()
                        : null)
                .createdAt(formatLocalDateTime(entity.getCreatedAt()))
                .updatedAt(formatLocalDateTime(entity.getUpdatedAt()))
                .orderItems(itemDtos)
                .build();
    }
}
