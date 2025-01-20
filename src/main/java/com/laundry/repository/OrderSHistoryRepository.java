package com.laundry.repository;

import com.laundry.entity.OrderStatus;
import com.laundry.entity.OrderStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderSHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    Page<OrderStatusHistory> findByOrderId(Long orderId, Pageable pageable);

    Page<OrderStatusHistory> findByOldStatus(OrderStatus oldStatus, Pageable pageable);

    Page<OrderStatusHistory> findByNewStatus(OrderStatus newStatus, Pageable pageable);

    Page<OrderStatusHistory> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable);

    Page<OrderStatusHistory> findByChangedBy(Long changedBy, Pageable pageable);
}

