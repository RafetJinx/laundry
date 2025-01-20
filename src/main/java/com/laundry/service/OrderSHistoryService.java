package com.laundry.service;

import com.laundry.dto.OrderSHistoryResponseDto;
import com.laundry.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderSHistoryService {

    OrderSHistoryResponseDto findById(Long id);

    Page<OrderSHistoryResponseDto> findByOrderId(Long orderId, Pageable pageable);

    Page<OrderSHistoryResponseDto> findByOldStatus(OrderStatus oldStatus, Pageable pageable);

    Page<OrderSHistoryResponseDto> findByNewStatus(OrderStatus newStatus, Pageable pageable);

    Page<OrderSHistoryResponseDto> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable);

    Page<OrderSHistoryResponseDto> findByChangedBy(Long changedBy, Pageable pageable);
}
