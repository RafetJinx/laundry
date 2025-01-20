package com.laundry.mapper;

import com.laundry.dto.*;
import com.laundry.entity.*;
import com.laundry.util.OrderUtil;

import java.util.List;

import static com.laundry.mapper.DateTimeUtil.formatLocalDateTime;

public class OrderMapper {

    /**
     * Builds an Order entity from the given OrderRequestDto, user entity,
     * and a list of found services. Automatically sets defaults for PaymentStatus
     * and OrderStatus if not provided, and throws BadRequestException if
     * an invalid enum string is given.
     */
    public static Order toEntity(OrderRequestDto requestDto,
                                 User user,
                                 List<Service> foundServices) {
        if (requestDto == null) return null;

        Order order = new Order();
        order.setUser(user);
        order.setCurrencyCode(requestDto.getCurrencyCode());
        order.setPaymentStatus(OrderUtil.parsePaymentStatus(requestDto.getPaymentStatus()));
        order.setStatus(OrderUtil.parseOrderStatus(requestDto.getOrderStatus()));

        if (requestDto.getOrderItems() != null && !requestDto.getOrderItems().isEmpty()) {
            List<OrderItem> items = OrderUtil.buildOrderItems(order, requestDto, foundServices);
            order.setOrderItems(items);
        }
        return order;
    }

    /**
     * Converts an Order entity to an OrderResponseDto.
     */
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
                // Return enums as String or you can store them as enums in DTO as well.
                .paymentStatus(order.getPaymentStatus() != null
                        ? order.getPaymentStatus().name()
                        : null)
                .orderStatus(order.getStatus() != null
                        ? order.getStatus().name()
                        : null)
                .createdAt(formatLocalDateTime(order.getCreatedAt()))
                .updatedAt(formatLocalDateTime(order.getUpdatedAt()))
                .orderItems(itemDtos)
                .build();
    }
}
