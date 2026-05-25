package com.aicontentgenerator.infrastructure.ratelimit.service;

import lombok.Builder;
import lombok.Getter;

/**
 * Internal result of a rate limit check.
 * Carries everything the filter needs to set response headers and decide
 * whether to allow or reject the request.
 *
 * Not exposed to controllers — stays inside the ratelimit module.
 */
@Getter
@Builder
public class RateLimitDecision {

    private final boolean allowed;
    private final long    capacity;
    private final long    remainingTokens;

    /** Seconds until the bucket refills enough to allow the next request. */
    private final long retryAfterSeconds;

    public static RateLimitDecision allowed(long capacity, long remaining, long retryAfterSeconds) {
        return RateLimitDecision.builder()
                .allowed(true)
                .capacity(capacity)
                .remainingTokens(remaining)
                .retryAfterSeconds(retryAfterSeconds)
                .build();
    }

    public static RateLimitDecision denied(long capacity, long retryAfterSeconds) {
        return RateLimitDecision.builder()
                .allowed(false)
                .capacity(capacity)
                .remainingTokens(0)
                .retryAfterSeconds(retryAfterSeconds)
                .build();
    }
}
