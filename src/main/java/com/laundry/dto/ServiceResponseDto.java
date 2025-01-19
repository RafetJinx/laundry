package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ServiceResponseDto {
    Long id;
    String name;
    String description;
    String createdAt;
    String updatedAt;
    List<ServicePriceResponseDto> prices;
}
