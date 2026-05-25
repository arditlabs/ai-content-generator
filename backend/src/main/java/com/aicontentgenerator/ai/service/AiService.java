package com.aicontentgenerator.ai.service;

import com.aicontentgenerator.ai.client.AiProviderClient;
import com.aicontentgenerator.ai.dto.GenerateRequest;
import com.aicontentgenerator.ai.dto.GenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI service — the single entry point for content generation within this application.
 *
 * Design notes:
 *  - Delegates actual API calls to AiProviderClient (strategy pattern).
 *  - Does NOT save content — that is the Content module's responsibility.
 *  - Exposes two methods:
 *      generate(GenerateRequest) → for the REST controller (returns full DTO)
 *      generateText(String)      → for the Scheduler (returns plain String)
 *    This avoids coupling the scheduler to the AI module's DTOs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final AiProviderClient aiProviderClient;

    /**
     * Generates content from a user request.
     * Used by AiController.
     */
    public GenerateResponse generate(GenerateRequest request) {
        log.debug("AI generation requested. Prompt: \"{}\"", request.getPrompt());

        String result = aiProviderClient.generate(request.getPrompt());

        return GenerateResponse.builder()
                .prompt(request.getPrompt())
                .result(result)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Low-level text generation — accepts a raw prompt, returns raw result.
     * Used by the Scheduler module to avoid DTO coupling.
     */
    public String generateText(String prompt) {
        log.debug("AI text generation requested by scheduler. Prompt: \"{}\"", prompt);
        return aiProviderClient.generate(prompt);
    }
}
