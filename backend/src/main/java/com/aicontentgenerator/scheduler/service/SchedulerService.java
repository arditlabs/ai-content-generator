package com.aicontentgenerator.scheduler.service;

import com.aicontentgenerator.common.exception.AppException;
import com.aicontentgenerator.common.exception.ErrorCode;
import com.aicontentgenerator.scheduler.dto.ScheduleRequest;
import com.aicontentgenerator.scheduler.dto.ScheduledJobResponse;
import com.aicontentgenerator.scheduler.entity.JobStatus;
import com.aicontentgenerator.scheduler.entity.ScheduledJob;
import com.aicontentgenerator.scheduler.repository.ScheduledJobRepository;
import com.aicontentgenerator.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler service — two responsibilities:
 *
 * 1. CRUD for scheduled jobs (via REST controller)
 * 2. Polling loop (every 10 seconds) that finds due jobs and processes them
 *
 * The processing itself is delegated to SchedulerJobExecutor so that each job
 * runs in its own @Transactional boundary (see SchedulerJobExecutor for details).
 *
 * Note on concurrency:
 *   Spring's default task scheduler is single-threaded. Combined with fixedDelay
 *   (which waits for the previous execution to finish before scheduling the next),
 *   processDueJobs() will never run concurrently on a single instance.
 *   For multi-instance deployments, a distributed lock (e.g. ShedLock) would
 *   be needed — out of scope for this MVP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchedulerService {

    private final ScheduledJobRepository scheduledJobRepository;
    private final SchedulerJobExecutor   jobExecutor;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public ScheduledJobResponse schedule(User user, ScheduleRequest request) {
        ScheduledJob job = ScheduledJob.builder()
                .user(user)
                .prompt(request.getPrompt())
                .runAt(request.getRunAt())
                .status(JobStatus.PENDING)
                .build();

        ScheduledJob saved = scheduledJobRepository.save(job);
        log.info("Job [{}] scheduled for [{}] by user [{}]",
                saved.getId(), saved.getRunAt(), user.getEmail());
        return ScheduledJobResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduledJobResponse> getAllByUser(User user) {
        return scheduledJobRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ScheduledJobResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduledJobResponse getByIdAndUser(Long id, User user) {
        return scheduledJobRepository.findByIdAndUser(id, user)
                .map(ScheduledJobResponse::from)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.JOB_NOT_FOUND));
    }

    public void cancel(Long id, User user) {
        ScheduledJob job = scheduledJobRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.JOB_NOT_FOUND));

        if (job.getStatus() != JobStatus.PENDING) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.JOB_NOT_CANCELLABLE);
        }

        scheduledJobRepository.delete(job);
        log.info("Job [{}] cancelled by user [{}]", id, user.getEmail());
    }

    // ── Polling loop ──────────────────────────────────────────────────────────

    /**
     * Runs every 10 seconds after the previous execution completes (fixedDelay).
     * Fetches all PENDING jobs whose run_at is now or in the past and processes them.
     *
     * Each job is processed independently via SchedulerJobExecutor so a single
     * failing job does not block the rest.
     */
    @Scheduled(fixedDelay = 10_000)
    public void processDueJobs() {
        List<ScheduledJob> dueJobs = scheduledJobRepository
                .findByStatusAndRunAtLessThanEqual(JobStatus.PENDING, LocalDateTime.now());

        if (dueJobs.isEmpty()) {
            return;
        }

        log.info("Scheduler: found {} due job(s) to process", dueJobs.size());

        for (ScheduledJob job : dueJobs) {
            jobExecutor.execute(job);
        }
    }
}
