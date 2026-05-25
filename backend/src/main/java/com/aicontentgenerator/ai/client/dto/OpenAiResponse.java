package com.aicontentgenerator.ai.client.dto;

import java.util.List;

/**
 * Internal DTO for the OpenAI Chat Completions API response.
 *
 * Only maps the fields we actually use.
 * Jackson ignores unmapped fields by default (Spring Boot default config).
 *
 * Example response shape:
 * {
 *   "choices": [
 *     { "message": { "role": "assistant", "content": "..." }, "finish_reason": "stop" }
 *   ]
 * }
 */
public record OpenAiResponse(List<Choice> choices) {

    public record Choice(Message message, String finish_reason) {}

    public record Message(String role, String content) {}

    /**
     * Convenience accessor — returns the first choice's content.
     * Returns null if choices list is empty.
     */
    public String firstChoiceContent() {
        if (choices == null || choices.isEmpty()) return null;
        Choice first = choices.get(0);
        if (first.message() == null) return null;
        return first.message().content();
    }
}
