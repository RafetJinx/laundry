package com.laundry.repository;

import com.laundry.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
