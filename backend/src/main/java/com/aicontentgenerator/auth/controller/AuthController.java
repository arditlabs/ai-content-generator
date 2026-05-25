package com.aicontentgenerator.auth.controller;


import com.aicontentgenerator.auth.dto.AuthResponse;
import com.aicontentgenerator.auth.dto.LoginRequest;
import com.aicontentgenerator.auth.dto.RefreshTokenRequest;
import com.aicontentgenerator.auth.dto.RegisterRequest;
import com.aicontentgenerator.auth.service.AuthService;
import com.aicontentgenerator.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth REST controller.
 *
 * All endpoints are public (whitelisted in SecurityConfig).
 *
 * POST /api/v1/auth/register  → create account, returns tokens
 * POST /api/v1/auth/login     → authenticate, returns tokens
 * POST /api/v1/auth/refresh   → exchange refresh token for new access token
 * POST /api/v1/auth/logout    → revoke refresh token
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}