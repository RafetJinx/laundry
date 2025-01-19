package com.laundry.service;

import com.laundry.dto.OrderRequestDto;
import com.laundry.dto.OrderResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto,
                                 Long currentUserId,
                                 String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    OrderResponseDto updateOrder(Long id,
                                 OrderRequestDto requestDto,
                                 Long currentUserId,
                                 String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    OrderResponseDto patchOrder(Long id,
                                OrderRequestDto requestDto,
                                Long currentUserId,
                                String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    OrderResponseDto getOrderById(Long id,
                                  Long currentUserId,
                                  String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    List<OrderResponseDto> getAllOrders(Long currentUserId,
                                        String currentUserRole)
            throws AccessDeniedException;

    void deleteOrder(Long id,
                     Long currentUserId,
                     String currentUserRole)
            throws NotFoundException, AccessDeniedException;
}
