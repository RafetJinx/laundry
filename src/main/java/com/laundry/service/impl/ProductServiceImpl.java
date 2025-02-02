package com.laundry.service.impl;

import com.laundry.dto.ProductRequestDto;
import com.laundry.dto.ProductResponseDto;
import com.laundry.entity.Product;
import com.laundry.exception.NotFoundException;
import com.laundry.helper.RoleGuard;
import com.laundry.mapper.ProductMapper;
import com.laundry.repository.ProductRepository;
import com.laundry.service.ProductService;
import com.laundry.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto dto, Long currentUserId, String currentUserRole) {
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to create a product");
        ProductRequestDto normalizedDto = normalizeProductNameInDto(dto);
        Product product = ProductMapper.toEntity(normalizedDto);
        productRepository.save(product);
        return ProductMapper.toResponseDto(product);
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto, Long currentUserId, String currentUserRole) {
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to update a product");
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        ProductRequestDto normalizedDto = normalizeProductNameInDto(dto);
        existing.setName(normalizedDto.getName());
        existing.setDescription(normalizedDto.getDescription());
        productRepository.save(existing);
        return ProductMapper.toResponseDto(existing);
    }

    @Override
    public ProductResponseDto getProductById(Long id, Long currentUserId, String currentUserRole) {
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to view product details");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        return ProductMapper.toResponseDto(product);
    }

    @Override
    public List<ProductResponseDto> getAllProducts(Long currentUserId, String currentUserRole) {
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to view all products");
        return productRepository.findAll()
                .stream()
                .map(ProductMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id, Long currentUserId, String currentUserRole) {
        RoleGuard.requireAdminRole(currentUserRole, "You do not have permission to delete a product");
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    public Page<ProductResponseDto> searchProducts(String name, String description, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Specification<Product> spec = Specification.where(null);
        if (name != null && !name.isEmpty()) {
            spec = spec.and(ProductSpecification.hasName(name));
        }
        if (description != null && !description.isEmpty()) {
            spec = spec.and(ProductSpecification.hasDescription(description));
        }
        if (startDate != null && endDate != null) {
            spec = spec.and(ProductSpecification.createdBetween(startDate, endDate));
        }
        return productRepository.findAll(spec, pageable).map(ProductMapper::toResponseDto);
    }

    /**
     * Normalizes the product name in the given DTO by applying the following rule:
     * - For each word in the name, if the word length is less than or equal to 2, convert the entire word to uppercase.
     * - Otherwise, set the first character to uppercase and the remaining characters to lowercase.
     *
     * @param dto the original ProductRequestDto
     * @return a new ProductRequestDto with a normalized name
     */
    private ProductRequestDto normalizeProductNameInDto(ProductRequestDto dto) {
        String name = dto.getName();
        if (name == null || name.isEmpty()) {
            return dto;
        }
        String normalizedName = normalizeProductName(name);
        return ProductRequestDto.builder()
                .name(normalizedName)
                .description(dto.getDescription())
                .build();
    }

    /**
     * Normalizes the product name.
     *
     * @param name the original product name
     * @return the normalized product name
     */
    private String normalizeProductName(String name) {
        String[] words = name.trim().split("\\s+");
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() <= 2) {
                words[i] = words[i].toUpperCase();
            } else {
                words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
            }
        }
        return String.join(" ", words);
    }
}
