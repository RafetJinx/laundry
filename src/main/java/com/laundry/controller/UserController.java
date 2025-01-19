package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.security.JwtUtil;
import com.laundry.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Validated @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        UserResponseDto updated = userService.updateUser(id, requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> patchUser(
            @PathVariable Long id,
            @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        UserResponseDto patched = userService.patchUser(id, requestDto, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("User patched successfully", patched));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        UserResponseDto user = userService.getUserById(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("User found", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);
        userService.deleteUser(id, currentUserId, currentUserRole);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
}
