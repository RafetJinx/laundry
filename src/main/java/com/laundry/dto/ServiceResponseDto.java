package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ServiceResponseDto {
    Long id;
    String name;
    String description;
    BigDecimal price;
    String currencyCode;
    String createdAt;
    String updatedAt;
}
