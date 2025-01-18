package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.exception.*;
import com.laundry.security.JwtUtil;
import com.laundry.service.UserService;
import com.laundry.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /*
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
            @Validated @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        try {
            // 1) Check if caller is already logged in
            //    If yes and not admin => 403 (they must logout first)
            boolean isLoggedIn = (authentication != null && authentication.isAuthenticated());
            boolean isAdmin = (isLoggedIn
                    && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

            if (isLoggedIn && !isAdmin) {
                // Non-admin user is already logged in => must logout first
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You must logout before creating a new user"));
            }

            // 2) Determine the final role for the new user
            //    Default to ROLE_USER
            String desiredRole = (requestDto.getRole() == null) ? "USER"
                    : requestDto.getRole().toUpperCase();

            // If the caller is admin, they can override to ROLE_ADMIN or ROLE_USER only.
            // If they attempt something else => 403 or 400
            if (isAdmin) {
                // Only "ROLE_USER" or "ROLE_ADMIN" allowed
                if (!desiredRole.equals("ROLE_USER") && !desiredRole.equals("ADMIN")) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("Admin can only set USER or ADMIN"));
                }
            } else {
                // Caller is not admin => force "ROLE_USER"
                desiredRole = "USER";
            }

            if (!EmailUtil.isEmailValid(requestDto.getEmail())) {
                throw new InvalidEmailException("Invalid email address");
            }

            // 3) Build a new requestDto with the final role
            UserRequestDto safeDto = UserRequestDto.builder()
                    .username(requestDto.getUsername())
                    .password(requestDto.getPassword())
                    .email(requestDto.getEmail())
                    .phone(requestDto.getPhone())
                    .address(requestDto.getAddress())
                    .displayName(requestDto.getDisplayName())
                    .role(desiredRole)
                    .build();

            UserResponseDto createdUser = userService.createUser(safeDto);

            return ResponseEntity.ok(
                    ApiResponse.success("User registered successfully", createdUser)
            );
        } catch (UserAlreadyExistsException e) {
            log.error("Registration conflict: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration error: " + e.getMessage()));
        }
    }
    */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Validated @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        try {
            Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
            String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

            UserResponseDto updated = userService.updateUser(id, requestDto, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", updated));
        } catch (NotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (UserAlreadyExistsException e) {
            log.error("User conflict", e);
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Forbidden", e);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> patchUser(
            @PathVariable Long id,
            @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        try {
            Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
            String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

            UserResponseDto patched = userService.patchUser(id, requestDto, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success("User patched successfully", patched));
        } catch (NotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (UserAlreadyExistsException e) {
            log.error("User conflict", e);
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Forbidden", e);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
            String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

            UserResponseDto user = userService.getUserById(id, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success("User found", user));
        } catch (NotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Forbidden", e);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
            String currentUserRole = JwtUtil.getRoleFromAuthentication(authentication);

            userService.deleteUser(id, currentUserId, currentUserRole);
            return ResponseEntity.ok(ApiResponse.success("User deleted", null));
        } catch (NotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            log.error("Forbidden", e);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
