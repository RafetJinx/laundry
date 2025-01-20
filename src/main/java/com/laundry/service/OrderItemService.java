package com.laundry.service;

import com.laundry.dto.OrderItemRequestDto;
import com.laundry.dto.OrderItemResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;

import java.util.List;

public interface OrderItemService {

    OrderItemResponseDto createOrderItem(Long orderId,
                                         OrderItemRequestDto requestDto,
                                         Long currentUserId,
                                         String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    OrderItemResponseDto updateOrderItem(Long itemId,
                                         OrderItemRequestDto requestDto,
                                         Long currentUserId,
                                         String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    OrderItemResponseDto getOrderItem(Long itemId,
                                      Long currentUserId,
                                      String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    List<OrderItemResponseDto> getItemsByOrder(Long orderId,
                                               Long currentUserId,
                                               String currentUserRole)
            throws NotFoundException, AccessDeniedException;

    void deleteOrderItem(Long itemId,
                         Long currentUserId,
                         String currentUserRole)
            throws NotFoundException, AccessDeniedException;
}
