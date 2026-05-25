package com.aicontentgenerator.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * AI module configuration.
 *
 * The OpenAI RestClient bean is only created when provider=openai,
 * so the mock bean can run in development with zero external dependencies.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    /**
     * Dedicated RestClient for OpenAI — scoped to this module only.
     * Created only when app.ai.provider=openai is set.
     */
    @Bean("openAiRestClient")
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
    public RestClient openAiRestClient(AiProperties properties) {
        AiProperties.OpenAi openAi = properties.getOpenai();
        return RestClient.builder()
                .baseUrl(openAi.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + openAi.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
