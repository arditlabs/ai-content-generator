package com.aicontentgenerator.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration for the AI module.
 * Bound from application.yml under the "app.ai" prefix.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    /** Which provider to use: "mock" (default) or "openai" */
    private String provider = "mock";

    private OpenAi openai = new OpenAi();

    @Getter
    @Setter
    public static class OpenAi {
        private String apiKey;
        private String baseUrl     = "https://api.openai.com/v1";
        private String model       = "gpt-4o-mini";
        private int    maxTokens   = 1000;
        private String systemPrompt = "You are a professional content writer. "
                + "Generate clear, engaging, and well-structured content based on the user's prompt.";
    }
}
