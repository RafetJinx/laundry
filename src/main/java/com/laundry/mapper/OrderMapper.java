package com.laundry.mapper;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.dto.OrderRequestDto;
import com.laundry.dto.OrderResponseDto;
import com.laundry.entity.Order;
import com.laundry.entity.OrderItem;
import com.laundry.entity.Service;
import com.laundry.entity.User;
import java.util.ArrayList;
import java.util.List;

import static com.laundry.mapper.DateTimeUtil.formatLocalDateTime;

public class OrderMapper {

    /**
     * create/update yapılırken:
     * - "user" entity’yi bulmak
     * - "service" entity’leri bulmak (orderItems üzerinden)
     * Service katmanında bu mapper'a user & service entity'lerini
     * parametre olarak iletebilirsiniz.
     */
    public static Order toEntity(OrderRequestDto dto, User user,
                                 List<Service> foundServices) {
        if (dto == null) {
            return null;
        }
        Order order = new Order();
        order.setUser(user);  // parametre
        order.setTotalAmount(dto.getTotalAmount());
        order.setCurrencyCode(dto.getCurrencyCode());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setOrderStatus(dto.getOrderStatus());

        // OrderItem'lar
        if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            List<OrderItem> itemEntities = new ArrayList<>();

            // dtodaki orderItems -> entity'ye çevir
            for (OrderItemRequestDto itemDto : dto.getOrderItems()) {
                // serviceId => bulmak
                Service matchedService = foundServices.stream()
                        .filter(svc -> svc.getId().equals(itemDto.getServiceId()))
                        .findFirst()
                        .orElse(null);

                OrderItem itemEntity = OrderItemMapper.toEntity(itemDto, order, matchedService);
                itemEntities.add(itemEntity);
            }
            order.setOrderItems(itemEntities);
        }
        return order;
    }

    public static OrderResponseDto toResponseDto(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDto> itemDtos = null;
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            itemDtos = order.getOrderItems().stream()
                    .map(OrderItemMapper::toResponseDto)
                    .toList();
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .totalAmount(order.getTotalAmount())
                .currencyCode(order.getCurrencyCode())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .createdAt(formatLocalDateTime(order.getCreatedAt()))
                .updatedAt(formatLocalDateTime(order.getUpdatedAt()))
                .orderItems(itemDtos)
                .build();
    }
}
