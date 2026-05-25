package com.aicontentgenerator.scheduler.dto;

import com.aicontentgenerator.scheduler.entity.JobStatus;
import com.aicontentgenerator.scheduler.entity.ScheduledJob;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduledJobResponse {

    private final Long          id;
    private final String        prompt;
    private final LocalDateTime runAt;
    private final JobStatus     status;
    private final LocalDateTime createdAt;

    public static ScheduledJobResponse from(ScheduledJob job) {
        return ScheduledJobResponse.builder()
                .id(job.getId())
                .prompt(job.getPrompt())
                .runAt(job.getRunAt())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .build();
    }
}
