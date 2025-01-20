package com.laundry.service;

import com.laundry.dto.OrderSHistoryResponseDto;
import com.laundry.entity.OrderStatus;
import com.laundry.exception.NotFoundException;
import com.laundry.mapper.OrderSHistoryMapper;
import com.laundry.repository.OrderSHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderSHistoryServiceImpl implements OrderSHistoryService {

    @Autowired
    private OrderSHistoryRepository orderSHistoryRepository;

    @Override
    public OrderSHistoryResponseDto findById(Long id) {
        return orderSHistoryRepository.findById(id)
                .map(OrderSHistoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Order status history not found: " + id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSHistoryResponseDto> findByOrderId(Long orderId, Pageable pageable) {
        return orderSHistoryRepository.findByOrderId(orderId, pageable)
                .map(OrderSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSHistoryResponseDto> findByOldStatus(OrderStatus oldStatus, Pageable pageable) {
        return orderSHistoryRepository.findByOldStatus(oldStatus, pageable)
                .map(OrderSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSHistoryResponseDto> findByNewStatus(OrderStatus newStatus, Pageable pageable) {
        return orderSHistoryRepository.findByNewStatus(newStatus, pageable)
                .map(OrderSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSHistoryResponseDto> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable) {
        return orderSHistoryRepository.findByChangedAtBetween(changedAtAfter, changedAtBefore, pageable)
                .map(OrderSHistoryMapper::toDto);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderSHistoryResponseDto> findByChangedBy(Long changedBy, Pageable pageable) {
        return orderSHistoryRepository.findByChangedBy(changedBy, pageable)
                .map(OrderSHistoryMapper::toDto);
    }
}
