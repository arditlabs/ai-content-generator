package com.aicontentgenerator.infrastructure.ratelimit.factory;

/**
 * The three rate limit tiers, mapped to endpoint paths in RateLimitFilter.
 *
 *  AUTH    → /api/v1/auth/**        (brute-force protection)
 *  AI      → /api/v1/ai/**         (expensive provider calls)
 *  GENERAL → everything else        (standard API traffic)
 */
public enum BucketType {
    AUTH,
    AI,
    GENERAL
}
