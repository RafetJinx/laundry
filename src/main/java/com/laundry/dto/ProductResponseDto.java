package com.laundry.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponseDto {
    Long id;
    String name;
    String description;
    String createdAt;
    String updatedAt;
}
