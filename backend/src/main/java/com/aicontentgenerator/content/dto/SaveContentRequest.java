package com.aicontentgenerator.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaveContentRequest {

    @NotBlank(message = "Prompt is required")
    @Size(min = 3, max = 2000, message = "Prompt must be between 3 and 2000 characters")
    private String prompt;

    @NotBlank(message = "Result is required")
    private String result;
}
