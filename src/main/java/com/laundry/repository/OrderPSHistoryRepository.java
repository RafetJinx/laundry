package com.laundry.repository;

import com.laundry.entity.OrderPaymentStatusHistory;
import com.laundry.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderPSHistoryRepository extends JpaRepository<OrderPaymentStatusHistory, Long> {

    Page<OrderPaymentStatusHistory> findByOrderId(Long orderId, Pageable pageable);

    Page<OrderPaymentStatusHistory> findByOldPaymentStatus(PaymentStatus oldPaymentStatus, Pageable pageable);

    Page<OrderPaymentStatusHistory> findByNewPaymentStatus(PaymentStatus newPaymentStatus, Pageable pageable);

    Page<OrderPaymentStatusHistory> findByChangedAtBetween(LocalDateTime changedAtAfter, LocalDateTime changedAtBefore, Pageable pageable);

    Page<OrderPaymentStatusHistory> findByChangedBy(Long changedBy, Pageable pageable);

}
