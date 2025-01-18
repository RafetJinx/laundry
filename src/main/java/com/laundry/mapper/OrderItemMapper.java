package com.laundry.mapper;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.entity.OrderItem;
import com.laundry.entity.Order;
import com.laundry.entity.Service;

import static com.laundry.mapper.DateTimeUtil.formatLocalDateTime;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemRequestDto dto, Order order, Service service) {
        if (dto == null) {
            return null;
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setService(service);
        item.setPriceAmount(dto.getPriceAmount());
        item.setQuantity(dto.getQuantity());
        return item;
    }

    public static OrderItemResponseDto toResponseDto(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponseDto.builder()
                .id(item.getId())
                .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
                .serviceId(item.getService() != null ? item.getService().getId() : null)
                .priceAmount(item.getPriceAmount())
                .quantity(item.getQuantity())
                .createdAt(formatLocalDateTime(item.getCreatedAt()))
                .updatedAt(formatLocalDateTime(item.getUpdatedAt()))
                .build();
    }
}
