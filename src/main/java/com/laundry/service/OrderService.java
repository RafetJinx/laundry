package com.laundry.service;

import com.laundry.dto.OrderRequestDto;
import com.laundry.dto.OrderResponseDto;
import com.laundry.exception.AccessDeniedException;
import com.laundry.exception.BadRequestException;
import com.laundry.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto,
                                 Long currentUserId,
                                 String currentUserRole)
            throws Exception;

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

    OrderResponseDto advanceOrderStatus(Long orderId,
                                        Long currentUserId,
                                        String currentUserRole)
            throws NotFoundException, AccessDeniedException, BadRequestException;

    Page<OrderResponseDto> searchOrders(
            Long userId,
            com.laundry.entity.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String keyword,
            Pageable pageable);

    void printOrder(Long orderId, Long currentUserId, String currentUserRole) throws Exception;

}
