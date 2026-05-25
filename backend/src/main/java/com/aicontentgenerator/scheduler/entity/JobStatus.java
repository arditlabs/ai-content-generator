package com.aicontentgenerator.scheduler.entity;

/**
 * Lifecycle states for a scheduled job.
 *
 * Transitions:
 *   PENDING → DONE   (successful AI generation + content save)
 *   PENDING → FAILED (AI generation error or content save error)
 *
 * DONE and FAILED are terminal states — no further transitions.
 * Only PENDING jobs can be cancelled via the API.
 */
public enum JobStatus {
    PENDING,
    DONE,
    FAILED
}
