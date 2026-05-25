package com.aicontentgenerator.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final String email;

    /** Convenience factory to avoid repeating tokenType = "Bearer" everywhere. */
    public static AuthResponse of(String accessToken, String refreshToken, String email) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .email(email)
                .build();
    }
}