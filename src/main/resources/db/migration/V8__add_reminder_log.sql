CREATE TABLE reminder_log (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES app_user(id),
    match_id   BIGINT       NOT NULL REFERENCES match(id),
    sent_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reminder_log_user_match UNIQUE (user_id, match_id)
);
