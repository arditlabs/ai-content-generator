package com.aicontentgenerator.scheduler.service;

import com.aicontentgenerator.ai.service.AiService;
import com.aicontentgenerator.content.service.ContentService;
import com.aicontentgenerator.scheduler.entity.JobStatus;
import com.aicontentgenerator.scheduler.entity.ScheduledJob;
import com.aicontentgenerator.scheduler.repository.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Executes a single scheduled job within its own database transaction.
 *
 * WHY a separate component?
 * Spring's @Transactional works through AOP proxies. A method annotated with
 * @Transactional called from WITHIN the same class (self-invocation) bypasses
 * the proxy and therefore runs without a transaction.
 *
 * By extracting job execution into this dedicated component, SchedulerService
 * can call execute() on the proxy — guaranteeing correct transactional behaviour
 * for each job independently.
 *
 * Result: if job A fails, its FAILED status is committed and jobs B, C, D
 * continue unaffected in their own transactions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerJobExecutor {

    private final AiService               aiService;
    private final ContentService          contentService;
    private final ScheduledJobRepository  scheduledJobRepository;

    /**
     * Processes one job: generates AI content, persists it, and marks the job DONE.
     * Any failure marks the job FAILED. The transaction commits in both cases.
     */
    @Transactional
    public void execute(ScheduledJob job) {
        log.info("Executing scheduled job [{}] for user [{}] — prompt: \"{}\"",
                job.getId(), job.getUser().getEmail(), job.getPrompt());
        try {
            String result = aiService.generateText(job.getPrompt());
            contentService.save(job.getUser(), job.getPrompt(), result);
            job.setStatus(JobStatus.DONE);
            log.info("Scheduled job [{}] completed successfully", job.getId());
        } catch (Exception ex) {
            job.setStatus(JobStatus.FAILED);
            log.error("Scheduled job [{}] failed: {}", job.getId(), ex.getMessage());
        } finally {
            // Status update (DONE or FAILED) is always committed
            scheduledJobRepository.save(job);
        }
    }
}
