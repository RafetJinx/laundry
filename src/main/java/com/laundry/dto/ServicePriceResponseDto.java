package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ServicePriceResponseDto {
    Long id;
    BigDecimal price;
    String currencyCode;
    String createdAt;
    String updatedAt;
}
