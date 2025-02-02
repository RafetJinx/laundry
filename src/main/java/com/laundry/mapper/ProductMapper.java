package com.laundry.mapper;

import com.laundry.dto.ProductRequestDto;
import com.laundry.dto.ProductResponseDto;
import com.laundry.entity.Product;
import com.laundry.util.DateTimeUtil;

public class ProductMapper {

    public static Product toEntity(ProductRequestDto dto) {
        if (dto == null) return null;
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        return product;
    }

    public static ProductResponseDto toResponseDto(Product product) {
        if (product == null) return null;
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .createdAt(DateTimeUtil.formatLocalDateTime(product.getCreatedAt()))
                .updatedAt(DateTimeUtil.formatLocalDateTime(product.getUpdatedAt()))
                .build();
    }
}
