package com.laundry.service;

import com.laundry.dto.OrderPSHistoryResponseDto;
import com.laundry.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface OrderPSHistoryService {

    OrderPSHistoryResponseDto findById(Long id);

    Page<OrderPSHistoryResponseDto> findByOrderId(Long orderId, Pageable pageable);

    Page<OrderPSHistoryResponseDto> findByOldPaymentStatus(PaymentStatus oldPaymentStatus, Pageable pageable);

    Page<OrderPSHistoryResponseDto> findByNewPaymentStatus(PaymentStatus newPaymentStatus, Pageable pageable);

    Page<OrderPSHistoryResponseDto> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable);

    Page<OrderPSHistoryResponseDto> findByChangedBy(Long changedBy, Pageable pageable);
}
