-- Add group scope to predictions (SPEC §7 / openapi.yaml: predictions are per user+group+match)

ALTER TABLE prediction
    ADD COLUMN group_id BIGINT NOT NULL REFERENCES app_group(id) ON DELETE CASCADE;

ALTER TABLE prediction
    DROP CONSTRAINT uq_prediction;

ALTER TABLE prediction
    ADD CONSTRAINT uq_prediction UNIQUE (user_id, group_id, match_id);

CREATE INDEX idx_prediction_group ON prediction(group_id);
