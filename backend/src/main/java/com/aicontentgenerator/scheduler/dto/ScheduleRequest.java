package com.aicontentgenerator.scheduler.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ScheduleRequest {

    @NotBlank(message = "Prompt is required")
    @Size(min = 3, max = 2000, message = "Prompt must be between 3 and 2000 characters")
    private String prompt;

    @NotNull(message = "Run time is required")
    @Future(message = "Run time must be in the future")
    private LocalDateTime runAt;
}
