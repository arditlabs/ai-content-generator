package com.aicontentgenerator.ai.client;

public interface AiProviderClient {

    /**
     * Sends a prompt to the AI provider and returns the generated text.
     *
     * @param prompt the user's input prompt
     * @return the generated content string
     * @throws com.aicontentgenerator.common.exception.AppException on provider failure
     */
    String generate(String prompt);
}
