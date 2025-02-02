package com.laundry.controller;

import com.laundry.dto.*;
import com.laundry.exception.*;
import com.laundry.security.JwtUtil;
import com.laundry.security.LaundryUserDetailsService;
import com.laundry.service.UserService;
import com.laundry.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final UserService userService;

    private final JwtUtil jwtUtil;

    private final LaundryUserDetailsService userDetailsService;

    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          LaundryUserDetailsService userDetailsService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
            @Validated @RequestBody UserRequestDto requestDto,
            Authentication authentication
    ) {
        var desiredRole = getString(requestDto, authentication);
        if (!EmailUtil.isEmailValid(requestDto.getEmail())) {
            throw new InvalidEmailException("Invalid email address");
        }
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
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", createdUser));
    }

    private static String getString(UserRequestDto requestDto, Authentication authentication) {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        boolean isAdmin = isLoggedIn && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (isLoggedIn && !isAdmin) {
            throw new AccessDeniedException("You must logout before creating a new user");
        }
        String desiredRole = requestDto.getRole() == null ? "USER" : requestDto.getRole().toUpperCase();
        if (isAdmin) {
            if (!desiredRole.equals("USER") && !desiredRole.equals("ADMIN")) {
                throw new BadRequestException("Admin can only set USER or ADMIN");
            }
        } else {
            desiredRole = "USER";
        }
        return desiredRole;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> loginUser(
            @Validated @RequestBody LoginRequestDto loginRequest,
            Authentication authentication
    ) {
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated();
        boolean isAdmin = isLoggedIn && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (isLoggedIn && !isAdmin) {
            throw new AccessDeniedException("Already logged in. Please logout first.");
        }
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        authenticationManager.authenticate(authToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String token = jwtUtil.generateToken(userDetails);
        LoginResponseDto resp = LoginResponseDto.builder()
                .username(userDetails.getUsername())
                .token(token)
                .build();
        return ResponseEntity.ok(ApiResponse.success("Login successful", resp));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadRequestException("You are not logged in, cannot logout");
        }
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BadRequestException("No valid token provided to logout");
        }
        return ResponseEntity.ok(ApiResponse.success("User logged out.", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestParam("email") String email) {
        userService.initiatePasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success("If the email is registered, a password reset link has been sent", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword
    ) {
        userService.resetPasswordWithToken(token, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("You must be logged in to change password");
        }
        Long currentUserId = JwtUtil.getUserIdFromAuthentication(authentication);
        userService.changePasswordLoggedIn(currentUserId, oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @GetMapping("/check-auth-status")
    public ResponseEntity<ApiResponse<?>> checkAuthStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.success("Not logged in", null));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Logged in as: " + authentication.getName(), null));
        }
    }
}
