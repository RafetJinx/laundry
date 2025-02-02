package com.laundry.controller;

import com.laundry.dto.ApiResponse;
import com.laundry.dto.UserResponseDto;
import com.laundry.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users fetched", users));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        UserResponseDto user = adminUserService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User found", user));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request
    ) {
        UserResponseDto updated = adminUserService.updateUserRole(id, request.newRole());
        return ResponseEntity.ok(ApiResponse.success("User role updated", updated));
    }

    @GetMapping("/role")
    public ResponseEntity<ApiResponse<Page<UserResponseDto>>> getAllUsersByRole(
            @RequestParam String role,
            Pageable pageable
    ) {
        Page<UserResponseDto> users = adminUserService.getAllUsersByRole(role, pageable);
        return ResponseEntity.ok(ApiResponse.success("Users fetched by role", users));
    }

    public record RoleUpdateRequest(String newRole) {
    }
}
