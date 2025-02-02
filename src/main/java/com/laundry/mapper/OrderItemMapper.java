package com.laundry.mapper;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import com.laundry.entity.Service;

import static com.laundry.util.DateTimeUtil.formatLocalDateTime;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemRequestDto dto,
                                     Order order,
                                     Service service) {
        if (dto == null) {
            return null;
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setService(service);
        item.setPriceAmount(dto.getPriceAmount());
        item.setQuantity(dto.getQuantity());
        item.setWeight(dto.getWeight());
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
                .weight(entity.getWeight())
                .createdAt(formatLocalDateTime(entity.getCreatedAt()))
                .updatedAt(formatLocalDateTime(entity.getUpdatedAt()))
                .build();
    }
}
