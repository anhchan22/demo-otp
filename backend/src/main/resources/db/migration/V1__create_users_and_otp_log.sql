CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'UNVERIFIED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE otp_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    purpose VARCHAR(30) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INT NOT NULL DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    invalidated_at TIMESTAMP NULL,
    CONSTRAINT fk_otp_log_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_otp_user_purpose_status
    ON otp_log(user_id, purpose, status);

CREATE INDEX idx_otp_created_at
    ON otp_log(created_at);

CREATE INDEX idx_otp_expires_at
    ON otp_log(expires_at);
