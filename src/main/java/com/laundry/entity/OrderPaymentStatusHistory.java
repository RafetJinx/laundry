package com.laundry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_payment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_payment_status", nullable = false)
    private PaymentStatus oldPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_payment_status", nullable = false)
    private PaymentStatus newPaymentStatus;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private Long changedBy;
}
