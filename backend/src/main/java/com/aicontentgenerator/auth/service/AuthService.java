package com.aicontentgenerator.auth.service;



import com.aicontentgenerator.auth.dto.AuthResponse;
import com.aicontentgenerator.auth.dto.LoginRequest;
import com.aicontentgenerator.auth.dto.RefreshTokenRequest;
import com.aicontentgenerator.auth.dto.RegisterRequest;
import com.aicontentgenerator.auth.entity.RefreshToken;
import com.aicontentgenerator.auth.repository.RefreshTokenRepository;
import com.aicontentgenerator.common.exception.AppException;
import com.aicontentgenerator.common.exception.ErrorCode;
import com.aicontentgenerator.user.entity.User;
import com.aicontentgenerator.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Orchestrates all authentication flows.
 *
 * Design decisions:
 *  - Refresh tokens are stored in DB for revocability (logout, rotation).
 *  - On every login, existing refresh tokens for that user are revoked
 *    (single-session per user; easy to relax if multi-device support is needed).
 *  - Token rotation: each /refresh call issues a new refresh token and
 *    deletes the old one.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository        userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;
    private final AuthenticationManager  authenticationManager;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    // ── Register ──────────────────────────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(HttpStatus.CONFLICT, ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        // Delegates credential verification to Spring Security.
        // Throws BadCredentialsException on failure → caught by GlobalExceptionHandler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

        // Revoke previous sessions
        refreshTokenRepository.deleteAllByUser(user);

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new AppException(HttpStatus.UNAUTHORIZED, ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = stored.getUser();

        // Token rotation: invalidate old, issue new
        refreshTokenRepository.delete(stored);

        log.info("Token refreshed for user: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken)
                .ifPresent(refreshTokenRepository::delete);
        log.info("Refresh token revoked");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user).getToken();
        return AuthResponse.of(accessToken, refreshToken, user.getEmail());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000))
                .build();
        return refreshTokenRepository.save(token);
    }
}