package com.aicontentgenerator.infrastructure.ratelimit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Activates RateLimitProperties binding from application.yml.
 * Follows the same pattern as AiConfig → @EnableConfigurationProperties.
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {
}
