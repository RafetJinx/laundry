package com.laundry.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "service_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicePrice extends AuditableBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;
}
