package com.aicontentgenerator.infrastructure.ratelimit.service;

import com.aicontentgenerator.infrastructure.ratelimit.config.RateLimitProperties;
import com.aicontentgenerator.infrastructure.ratelimit.factory.BucketFactory;
import com.aicontentgenerator.infrastructure.ratelimit.factory.BucketType;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Manages the per-client bucket cache and exposes a single tryConsume() method.
 *
 * Cache key format:
 *   "user:alice@example.com:AI"     — authenticated user, AI tier
 *   "ip:192.168.1.1:GENERAL"        — anonymous IP, general tier
 *
 * MEMORY NOTE:
 *   ConcurrentHashMap is correct for single-instance deployment.
 *   For production, replace with Caffeine (adds TTL-based eviction) before Redis.
 *
 * UPGRADE PATH → Redis (Bucket4j + Lettuce):
 *   private final LettuceBasedProxyManager<String> proxyManager;
 *
 *   public RateLimitDecision tryConsume(String clientKey, BucketType type) {
 *       BucketConfiguration config = buildConfig(type);
 *       Bucket bucket = proxyManager.builder().build(clientKey + ":" + type, () -> config);
 *       ...
 *   }
 *   Zero changes to RateLimitFilter or BucketFactory.
 *
 * UPGRADE PATH → SaaS tier quotas (free/premium):
 *   Inject UserTierService, look up user plan, call bucketFactory.createBucket(type, tier).
 *   BucketFactory selects the right Limit based on (BucketType, Tier) combination.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final BucketFactory       bucketFactory;
    private final RateLimitProperties properties;

    private final ConcurrentHashMap<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    /**
     * Attempts to consume one token from the client's bucket.
     *
     * @param clientKey  "user:<email>" or "ip:<address>"
     * @param type       endpoint tier (AUTH / AI / GENERAL)
     * @return           decision carrying allow/deny + header values
     */
    public RateLimitDecision tryConsume(String clientKey, BucketType type) {
        String    cacheKey = clientKey + ":" + type.name();
        Bucket    bucket   = bucketCache.computeIfAbsent(cacheKey, k -> bucketFactory.createBucket(type));
        long      capacity = capacityFor(type);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        long retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

        if (probe.isConsumed()) {
            log.debug("Rate limit OK — key={} remaining={}", cacheKey, probe.getRemainingTokens());
            return RateLimitDecision.allowed(capacity, probe.getRemainingTokens(), retryAfterSeconds);
        }

        log.warn("Rate limit EXCEEDED — key={} retryAfter={}s", cacheKey, retryAfterSeconds);
        return RateLimitDecision.denied(capacity, retryAfterSeconds);
    }

    private long capacityFor(BucketType type) {
        return switch (type) {
            case AUTH    -> properties.getAuth().getCapacity();
            case AI      -> properties.getAi().getCapacity();
            case GENERAL -> properties.getGeneral().getCapacity();
        };
    }
}
