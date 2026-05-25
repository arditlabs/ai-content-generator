package com.aicontentgenerator.scheduler.repository;

import com.aicontentgenerator.scheduler.entity.JobStatus;
import com.aicontentgenerator.scheduler.entity.ScheduledJob;
import com.aicontentgenerator.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {

    /**
     * Core scheduler query — fetches all jobs that are due for processing.
     * The composite index on (status, run_at) in V2 migration makes this fast.
     */
    List<ScheduledJob> findByStatusAndRunAtLessThanEqual(JobStatus status, LocalDateTime runAt);

    /** All jobs for a user, most recent first. */
    List<ScheduledJob> findByUserOrderByCreatedAtDesc(User user);

    /** User-scoped lookup — prevents one user from accessing another's jobs. */
    Optional<ScheduledJob> findByIdAndUser(Long id, User user);
}
