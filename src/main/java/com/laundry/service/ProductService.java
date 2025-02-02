package com.laundry.service;

import com.laundry.dto.ProductRequestDto;
import com.laundry.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductService {

    ProductResponseDto createProduct(ProductRequestDto dto, Long currentUserId, String currentUserRole);

    ProductResponseDto updateProduct(Long id, ProductRequestDto dto, Long currentUserId, String currentUserRole);

    ProductResponseDto getProductById(Long id, Long currentUserId, String currentUserRole);

    List<ProductResponseDto> getAllProducts(Long currentUserId, String currentUserRole);

    void deleteProduct(Long id, Long currentUserId, String currentUserRole);

    Page<ProductResponseDto> searchProducts(String name, String description, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
