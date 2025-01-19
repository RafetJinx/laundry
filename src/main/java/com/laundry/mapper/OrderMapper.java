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

    public static Order toEntity(OrderRequestDto requestDto,
                                 User user,
                                 List<Service> foundServices) {
        if (requestDto == null) {
            return null;
        }
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(requestDto.getTotalAmount());
        order.setCurrencyCode(requestDto.getCurrencyCode());
        order.setPaymentStatus(
                requestDto.getPaymentStatus() != null
                        ? requestDto.getPaymentStatus()
                        : "PENDING"
        );
        order.setOrderStatus(
                requestDto.getOrderStatus() != null
                        ? requestDto.getOrderStatus()
                        : "CREATED"
        );

        if (requestDto.getOrderItems() != null && !requestDto.getOrderItems().isEmpty()) {
            List<OrderItem> orderItems = buildOrderItems(order, requestDto, foundServices);
            order.setOrderItems(orderItems);
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

    public static void updateEntity(Order existing,
                                    OrderRequestDto requestDto,
                                    List<Service> foundServices) {
        if (requestDto.getTotalAmount() != null) {
            existing.setTotalAmount(requestDto.getTotalAmount());
        }
        if (requestDto.getCurrencyCode() != null) {
            existing.setCurrencyCode(requestDto.getCurrencyCode());
        }
        if (requestDto.getPaymentStatus() != null) {
            existing.setPaymentStatus(requestDto.getPaymentStatus());
        }
        if (requestDto.getOrderStatus() != null) {
            existing.setOrderStatus(requestDto.getOrderStatus());
        }

        if (requestDto.getOrderItems() != null) {
            List<OrderItem> updatedItems = buildOrderItems(existing, requestDto, foundServices);
            existing.getOrderItems().clear();
            existing.getOrderItems().addAll(updatedItems);
        }
    }

    private static List<OrderItem> buildOrderItems(Order existing, OrderRequestDto requestDto, List<Service> foundServices) {
        List<OrderItem> updatedItems = new ArrayList<>();
        for (OrderItemRequestDto itemDto : requestDto.getOrderItems()) {
            Service matchedService = (foundServices == null) ? null
                    : foundServices.stream()
                    .filter(svc -> svc.getId().equals(itemDto.getServiceId()))
                    .findFirst()
                    .orElse(null);

            OrderItem itemEntity = OrderItemMapper.toEntity(itemDto, existing, matchedService);
            updatedItems.add(itemEntity);
        }
        return updatedItems;
    }

    public static void patchEntity(Order existing,
                                   OrderRequestDto requestDto,
                                   List<Service> foundServices) {
        updateEntity(existing, requestDto, foundServices);
    }
}
