package com.aicontentgenerator.infrastructure.ratelimit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configurable rate limit thresholds per endpoint tier.
 * Bound from application.yml under "app.rate-limit".
 *
 * Three tiers:
 *  auth    — strict   (brute-force protection on login/register)
 *  ai      — strictest (AI calls are expensive, protect the provider budget)
 *  general — normal   (standard API protection)
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private Limit auth    = new Limit(10,  10,  Duration.ofMinutes(1));
    private Limit ai      = new Limit(20,  20,  Duration.ofHours(1));
    private Limit general = new Limit(100, 100, Duration.ofMinutes(1));

    @Getter
    @Setter
    public static class Limit {

        /** Maximum tokens the bucket can hold. */
        private long capacity;

        /** Tokens restored per refill cycle. */
        private long refillTokens;

        /** How long one full refill cycle takes. */
        private Duration refillPeriod;

        public Limit(long capacity, long refillTokens, Duration refillPeriod) {
            this.capacity     = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriod = refillPeriod;
        }
    }
}
