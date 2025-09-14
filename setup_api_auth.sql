-- Make sure we are in the right database
CREATE DATABASE IF NOT EXISTS elder_main CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elder_main;

-- Create api_auth table if it does not exist
CREATE TABLE IF NOT EXISTS api_auth (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(128) NOT NULL UNIQUE,
    ip VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL DEFAULT NULL,
    INDEX (token),
    INDEX (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Remove any previous test token
DELETE FROM api_auth WHERE token = 'MYTOKEN';

-- Insert a fresh test token
INSERT INTO api_auth (token) VALUES ('MYTOKEN');
