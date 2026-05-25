package com.aicontentgenerator.infrastructure.ratelimit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * HTTP response body written when a request is rate-limited (HTTP 429).
 * Follows the same shape as ApiResponse for consistency,
 * but is a standalone class so the ratelimit module has no dependency
 * on the common module's ApiResponse.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RateLimitResponse {

    private final int           status;
    private final String        error;
    private final String        message;
    private final long          retryAfterSeconds;

    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}
