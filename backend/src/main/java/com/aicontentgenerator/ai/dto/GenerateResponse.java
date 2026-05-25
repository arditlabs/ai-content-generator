package com.aicontentgenerator.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GenerateResponse {

    /** The original prompt submitted by the user. */
    private final String prompt;

    /** The AI-generated content. */
    private final String result;

    /** Timestamp of when the generation completed. */
    private final LocalDateTime generatedAt;
}
