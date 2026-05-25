package com.aicontentgenerator.ai.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Internal DTO for the OpenAI Chat Completions API request body.
 * Using Java records — immutable, no boilerplate.
 *
 * Maps to: POST https://api.openai.com/v1/chat/completions
 */
public record OpenAiRequest(
        String model,
        List<Message> messages,
        @JsonProperty("max_tokens") int maxTokens
) {
    public record Message(String role, String content) {

        public static Message system(String content) {
            return new Message("system", content);
        }

        public static Message user(String content) {
            return new Message("user", content);
        }
    }
}
