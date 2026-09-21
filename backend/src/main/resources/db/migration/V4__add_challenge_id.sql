ALTER TABLE otp_log ADD COLUMN challenge_id CHAR(36) NULL AFTER id;
UPDATE otp_log SET challenge_id = UUID() WHERE challenge_id IS NULL;
ALTER TABLE otp_log MODIFY challenge_id CHAR(36) NOT NULL;
ALTER TABLE otp_log ADD UNIQUE INDEX idx_otp_challenge_id (challenge_id);
