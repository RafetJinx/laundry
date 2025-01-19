package com.laundry.mapper;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import com.laundry.entity.Service;

import static com.laundry.mapper.DateTimeUtil.formatLocalDateTime;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemRequestDto requestDto,
                                     Order order,
                                     Service service) {
        if (requestDto == null) {
            return null;
        }
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setService(service);
        item.setPriceAmount(requestDto.getPriceAmount());
        item.setQuantity(requestDto.getQuantity());
        return item;
    }

    public static OrderItemResponseDto toResponseDto(OrderItem entity) {
        if (entity == null) {
            return null;
        }
        return OrderItemResponseDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .serviceId(entity.getService() != null ? entity.getService().getId() : null)
                .priceAmount(entity.getPriceAmount())
                .quantity(entity.getQuantity())
                .createdAt(formatLocalDateTime(entity.getCreatedAt()))
                .updatedAt(formatLocalDateTime(entity.getUpdatedAt()))
                .build();
    }
}
