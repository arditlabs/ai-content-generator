package com.aicontentgenerator.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Centralized catalog of all application-level error codes.
 * Each module appends its own entries here, keeping error messages consistent.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Auth ────────────────────────────────────────────────────────────────
    EMAIL_ALREADY_EXISTS("Email address is already registered"),
    INVALID_CREDENTIALS("Invalid email or password"),
    INVALID_TOKEN("Token is invalid or has expired"),
    REFRESH_TOKEN_NOT_FOUND("Refresh token not found"),
    REFRESH_TOKEN_EXPIRED("Refresh token has expired — please log in again"),

    // ── User ────────────────────────────────────────────────────────────────
    USER_NOT_FOUND("User not found"),

    // ── AI ──────────────────────────────────────────────────────────────────
    AI_PROVIDER_ERROR("AI provider returned an error — please try again"),
    AI_EMPTY_RESPONSE("AI provider returned an empty response"),

    // ── Content ─────────────────────────────────────────────────────────────
    CONTENT_NOT_FOUND("Content not found"),

    // ── Scheduler ───────────────────────────────────────────────────────────
    JOB_NOT_FOUND("Scheduled job not found"),
    JOB_NOT_CANCELLABLE("Only PENDING jobs can be cancelled"),

    // ── General ─────────────────────────────────────────────────────────────
    UNAUTHORIZED("Authentication is required to access this resource"),
    FORBIDDEN("You do not have permission to perform this action"),
    INTERNAL_ERROR("An unexpected error occurred. Please try again later");

    private final String message;
}
