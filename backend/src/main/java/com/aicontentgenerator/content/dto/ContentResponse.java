package com.aicontentgenerator.content.dto;

import com.aicontentgenerator.content.entity.Content;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContentResponse {

    private final Long          id;
    private final String        prompt;
    private final String        result;
    private final LocalDateTime createdAt;

    /**
     * Maps a Content entity to a response DTO.
     * Keeps mapping logic colocated with the DTO — no separate mapper class needed at this scale.
     */
    public static ContentResponse from(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .prompt(content.getPrompt())
                .result(content.getResult())
                .createdAt(content.getCreatedAt())
                .build();
    }
}
