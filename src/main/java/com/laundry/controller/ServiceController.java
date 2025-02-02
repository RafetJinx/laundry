package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.security.JwtUtil;
import com.laundry.service.ServiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@Slf4j
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServiceResponseDto>> createService(
            @RequestBody ServiceRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        ServiceResponseDto created = serviceService.createService(requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Service created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponseDto>>> getAllServices() {
        List<ServiceResponseDto> services = serviceService.getAllServices();
        return ResponseEntity.ok(ApiResponse.success("All services fetched", services));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> getServiceById(@PathVariable Long id) {
        ServiceResponseDto serviceDto = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success("Service found", serviceDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> updateService(
            @PathVariable Long id,
            @RequestBody ServiceRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        ServiceResponseDto updated = serviceService.updateService(id, requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponseDto>> patchService(
            @PathVariable Long id,
            @RequestBody ServiceRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        ServiceResponseDto patched = serviceService.patchService(id, requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Service patched successfully", patched));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteService(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        serviceService.deleteService(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("Service deleted", null));
    }
}
