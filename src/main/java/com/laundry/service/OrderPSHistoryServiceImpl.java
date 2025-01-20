package com.laundry.service;

import com.laundry.dto.OrderPSHistoryResponseDto;
import com.laundry.entity.PaymentStatus;
import com.laundry.exception.NotFoundException;
import com.laundry.mapper.OrderPSHistoryMapper;
import com.laundry.repository.OrderPSHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderPSHistoryServiceImpl implements OrderPSHistoryService {

    @Autowired
    private OrderPSHistoryRepository orderPSHistoryRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public OrderPSHistoryResponseDto findById(Long id) {
        return orderPSHistoryRepository.findById(id)
                .map(OrderPSHistoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Order payment status history not found: " + id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderPSHistoryResponseDto> findByOrderId(Long orderId, Pageable pageable) {
        return orderPSHistoryRepository.findByOrderId(orderId, pageable)
                .map(OrderPSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderPSHistoryResponseDto> findByOldPaymentStatus(PaymentStatus oldPaymentStatus, Pageable pageable) {
        return orderPSHistoryRepository.findByOldPaymentStatus(oldPaymentStatus, pageable)
                .map(OrderPSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderPSHistoryResponseDto> findByNewPaymentStatus(PaymentStatus newPaymentStatus, Pageable pageable) {
        return orderPSHistoryRepository.findByNewPaymentStatus(newPaymentStatus, pageable)
                .map(OrderPSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderPSHistoryResponseDto> findByChangedAtBetween(LocalDateTime changedAtAfter,
                                                                  LocalDateTime changedAtBefore,
                                                                  Pageable pageable) {
        return orderPSHistoryRepository.findByChangedAtBetween(changedAtAfter, changedAtBefore, pageable)
                .map(OrderPSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderPSHistoryResponseDto> findByChangedBy(Long changedBy, Pageable pageable) {
        return orderPSHistoryRepository.findByChangedBy(changedBy, pageable)
                .map(OrderPSHistoryMapper::toDto);
    }
}
