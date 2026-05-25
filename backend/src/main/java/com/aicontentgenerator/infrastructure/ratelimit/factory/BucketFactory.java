package com.aicontentgenerator.infrastructure.ratelimit.factory;

import com.aicontentgenerator.infrastructure.ratelimit.config.RateLimitProperties;
import com.aicontentgenerator.infrastructure.ratelimit.config.RateLimitProperties.Limit;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Creates Bucket4j token-bucket instances configured per endpoint tier.
 *
 * Each bucket uses a greedy refill strategy:
 *  - tokens are restored continuously across the refill period
 *  - prevents bursting while allowing steady traffic
 *
 * UPGRADE PATH → Redis:
 *  Replace Bucket.builder() with a Bucket4j ProxyManager backed by
 *  LettuceBasedProxyManager. The Limit config stays identical —
 *  only the storage backend changes.
 */
@Component
@RequiredArgsConstructor
public class BucketFactory {

    private final RateLimitProperties properties;

    public Bucket createBucket(BucketType type) {
        Limit limit = switch (type) {
            case AUTH    -> properties.getAuth();
            case AI      -> properties.getAi();
            case GENERAL -> properties.getGeneral();
        };

        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(limit.getCapacity())
                .refillGreedy(limit.getRefillTokens(), limit.getRefillPeriod())
                .build();

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}
