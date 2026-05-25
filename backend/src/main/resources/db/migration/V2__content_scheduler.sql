-- ============================================================
-- V2: Content & Scheduler Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS content (
                                       id         BIGSERIAL PRIMARY KEY,
                                       user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt     TEXT      NOT NULL,
    result     TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_content_user_id
    ON content(user_id);

CREATE INDEX IF NOT EXISTS idx_content_created_at
    ON content(user_id, created_at DESC);

-- ============================================================

CREATE TABLE IF NOT EXISTS scheduled_jobs (
                                              id         BIGSERIAL PRIMARY KEY,
                                              user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt     TEXT NOT NULL,
    run_at     TIMESTAMP NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_scheduled_jobs_user_id
    ON scheduled_jobs(user_id);

CREATE INDEX IF NOT EXISTS idx_scheduled_jobs_status_run_at
    ON scheduled_jobs(status, run_at);