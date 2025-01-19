package com.laundry.repository;

import com.laundry.entity.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {

    boolean existsByServiceIdAndCurrencyCode(Long serviceId, String currencyCode);

    Optional<ServicePrice> findByServiceIdAndCurrencyCode(Long serviceId, String currencyCode);
}
