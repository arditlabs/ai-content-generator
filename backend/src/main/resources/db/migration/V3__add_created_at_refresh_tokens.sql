-- Add missing column required by JPA/Hibernate validation

ALTER TABLE refresh_tokens
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();