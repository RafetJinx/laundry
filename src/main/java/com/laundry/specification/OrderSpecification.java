package com.laundry.specification;

import com.laundry.entity.Order;
import com.laundry.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderSpecification {

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, builder) -> builder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Order> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, builder) -> builder.between(root.get("createdAt"), startDate, endDate);
    }

    public static Specification<Order> totalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount) {
        return (root, query, builder) -> builder.between(root.get("totalAmount"), minAmount, maxAmount);
    }

    public static Specification<Order> referenceNoContains(String keyword) {
        return (root, query, builder) -> builder.like(root.get("referenceNo"), "%" + keyword + "%");
    }
}
