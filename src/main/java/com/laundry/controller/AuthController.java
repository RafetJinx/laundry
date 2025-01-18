package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.exception.*;
import com.laundry.security.JwtUtil;
import com.laundry.security.LaundryUserDetailsService;
import com.laundry.service.UserService;
import com.laundry.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LaundryUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * 1) REGISTER a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
            @Validated @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        try {
            // 1) Check if caller is already logged in
            boolean isLoggedIn = (authentication != null && authentication.isAuthenticated());
            boolean isAdmin = (isLoggedIn
                    && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

            if (isLoggedIn && !isAdmin) {
                // Non-admin user is already logged in => must log out first
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("You must logout before creating a new user"));
            }

            // 2) Role logic
            String desiredRole = (requestDto.getRole() == null)
                    ? "USER"
                    : requestDto.getRole().toUpperCase();

            if (isAdmin) {
                if (!desiredRole.equals("USER") && !desiredRole.equals("ADMIN")) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error("Admin can only set USER or ADMIN"));
                }
            } else {
                desiredRole = "USER";
            }

            if (!EmailUtil.isEmailValid(requestDto.getEmail())) {
                throw new InvalidEmailException("Invalid email address");
            }

            // Build safe DTO
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

    /**
     * 2) LOGIN
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginUser(
            @Validated @RequestBody LoginRequestDto loginRequest,
            Authentication authentication
    ) {
        try {
            // Check if user is already logged in
            boolean isLoggedIn = (authentication != null && authentication.isAuthenticated());
            boolean isAdmin = (isLoggedIn
                    && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

            if (isLoggedIn && !isAdmin) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Already logged in. Please logout first."));
            }

            // normal login
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    );
            authenticationManager.authenticate(authToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            LoginResponseDto resp = LoginResponseDto.builder()
                    .username(userDetails.getUsername())
                    .token(token)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", resp));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login error: " + e.getMessage()));
        }
    }

    /**
     * 3) LOGOUT
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("You are not logged in, cannot logout"));
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("No valid token provided to logout"));
        }

        // If you have a tokenBlacklistService, you can do tokenBlacklistService.invalidateToken(token)
        return ResponseEntity.ok(
                ApiResponse.success("User logged out.", null)
        );
    }

    /**
     * 4) FORGOT PASSWORD => sends email with reset token
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestParam("email") String email) {
        try {
            userService.initiatePasswordReset(email);
            return ResponseEntity.ok(ApiResponse.success(
                    "Password reset link sent to your email", null));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Forgot password error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * 5) RESET PASSWORD => user clicks email link, we either show Thymeleaf page or
     * we let them POST new password to this endpoint with ?token=...
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword
    ) {
        try {
            userService.resetPasswordWithToken(token, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Reset password error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * 6) CHANGE PASSWORD => user is logged in, changes own password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Authentication authentication
    ) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("You must be logged in to change password"));
            }
            // get current user ID from principal
            Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
            userService.changePasswordLoggedIn(currentUserId, oldPassword, newPassword);

            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (AccessDeniedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (NotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Change password error", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error: " + e.getMessage()));
        }
    }

    /**
     * 7) CHECK AUTH STATUS => quick endpoint to see if user is logged in,
     *   and if so, who they are.
     */
    @GetMapping("/check-auth-status")
    public ResponseEntity<ApiResponse<?>> checkAuthStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.success("Not logged in", null));
        } else {
            // e.g. return the username
            return ResponseEntity.ok(ApiResponse.success(
                    "Logged in as: " + authentication.getName(), null));
        }
    }

}
