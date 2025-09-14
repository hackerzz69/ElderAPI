-- ==========================================
-- Zenyte Server Database Schema
-- Consolidated .sql file
-- Ordered for Foreign Key Integrity
-- Generated: 2025-09-09
-- ==========================================
-- Run with:
--   mysql -u username -p < schema.sql
-- ==========================================

-- Ensure database exists and switch to it
CREATE DATABASE IF NOT EXISTS elderapi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elder_forum;

-- ==========================================
-- Drop tables in reverse dependency order for clean rebuild
-- ==========================================
DROP TABLE IF EXISTS skill_hiscores;
DROP TABLE IF EXISTS asn_blacklist;
DROP TABLE IF EXISTS iptables_all;
DROP TABLE IF EXISTS players_online;
DROP TABLE IF EXISTS api_auth;
DROP TABLE IF EXISTS antiknox_cache;
DROP TABLE IF EXISTS logs_trades;
DROP TABLE IF EXISTS actions;
DROP TABLE IF EXISTS player_information;
DROP TABLE IF EXISTS discord_verifications;
DROP TABLE IF EXISTS votes;
DROP TABLE IF EXISTS user_credits;
DROP TABLE IF EXISTS store_payments;
DROP TABLE IF EXISTS store_purchases;
DROP TABLE IF EXISTS promotions;
DROP TABLE IF EXISTS advlog_pvp;
DROP TABLE IF EXISTS advlog_game;
DROP TABLE IF EXISTS awards_awarded;
DROP TABLE IF EXISTS awards_awards;
DROP TABLE IF EXISTS core_members;

-- ==========================================
-- Core Members
-- ==========================================
CREATE TABLE core_members (
    member_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,            -- forum/game username
    email VARCHAR(191) DEFAULT NULL,
    members_pass_hash VARCHAR(191) NOT NULL,     -- bcrypt hash
    member_group_id INT UNSIGNED NOT NULL DEFAULT 3, -- primary group ID
    mgroup_others VARCHAR(191) DEFAULT NULL,     -- comma-separated secondary groups
    mfa_details TEXT DEFAULT NULL,               -- JSON or serialized MFA info
    joined BIGINT UNSIGNED NOT NULL,             -- UNIX timestamp (seconds)
    ip_address VARCHAR(64) DEFAULT NULL,         -- registration IP
    last_visit BIGINT UNSIGNED NOT NULL,         -- UNIX timestamp (seconds)
    last_activity BIGINT UNSIGNED DEFAULT NULL,  -- UNIX timestamp (seconds)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Awards
-- ==========================================
CREATE TABLE awards_awards (
    award_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    award_title VARCHAR(191) NOT NULL,
    award_reason TEXT,
    award_desc TEXT,
    award_icon VARCHAR(191),
    award_cat_id INT UNSIGNED NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX (award_cat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE awards_awarded (
    awarded_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    awarded_member INT UNSIGNED NOT NULL,
    awarded_award INT UNSIGNED NOT NULL,
    awarded_date BIGINT NOT NULL, -- UNIX timestamp (seconds)
    awarded_options VARCHAR(32) NOT NULL DEFAULT 'manual',
    awarded_giver INT UNSIGNED DEFAULT NULL,     -- must allow NULL for ON DELETE SET NULL
    awarded_reason TEXT,
    awarded_cat INT UNSIGNED,
    awarded_title VARCHAR(191),
    INDEX (awarded_member),
    INDEX (awarded_award),
    INDEX (awarded_date),
    FOREIGN KEY (awarded_award) REFERENCES awards_awards(award_id)
        ON DELETE CASCADE,
    FOREIGN KEY (awarded_member) REFERENCES core_members(member_id)
        ON DELETE CASCADE,
    FOREIGN KEY (awarded_giver) REFERENCES core_members(member_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Adventure Logs
-- ==========================================
CREATE TABLE advlog_game (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(64) NOT NULL,
    icon VARCHAR(128) DEFAULT NULL,
    message TEXT NOT NULL,
    date BIGINT NOT NULL, -- UNIX timestamp (seconds)
    INDEX (user),
    INDEX (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE advlog_pvp (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(64) NOT NULL,
    icon VARCHAR(128) DEFAULT NULL,
    message TEXT NOT NULL,
    date BIGINT NOT NULL, -- UNIX timestamp (seconds)
    INDEX (user),
    INDEX (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Promotions / World Events
-- ==========================================
CREATE TABLE promotions (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    world VARCHAR(64) NOT NULL,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(191) NOT NULL,
    data TEXT,
    time TIMESTAMP NOT NULL,
    INDEX (world),
    INDEX (time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Store / Payments
-- ==========================================
CREATE TABLE store_purchases (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    item_name VARCHAR(191) NOT NULL,
    item_id INT UNSIGNED NOT NULL,
    quantity INT UNSIGNED NOT NULL DEFAULT 1,
    item_amount INT UNSIGNED NOT NULL DEFAULT 1,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    claimed TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (username),
    INDEX (claimed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE store_payments (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(191) NOT NULL,
    item_name VARCHAR(191) NOT NULL,
    paid DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    credit_amount INT UNSIGNED NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'Pending',
    client_ip VARCHAR(64) DEFAULT NULL,
    cvc_pass VARCHAR(64) DEFAULT NULL,
    zip_pass VARCHAR(64) DEFAULT NULL,
    address_pass VARCHAR(128) DEFAULT NULL,
    live_mode TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (username),
    INDEX (email),
    INDEX (status),
    INDEX (address_pass)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_credits (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    userid INT UNSIGNED NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL,
    credits INT UNSIGNED NOT NULL DEFAULT 0,
    total_credits INT UNSIGNED NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (userid) REFERENCES core_members(member_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Votes
-- ==========================================
CREATE TABLE votes (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    site VARCHAR(128) DEFAULT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    voted_on TIMESTAMP NULL DEFAULT NULL,
    claimed TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (username),
    INDEX (claimed),
    INDEX (voted_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Discord Verification
-- ==========================================
CREATE TABLE discord_verifications (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    discord_id BIGINT UNSIGNED NOT NULL,
    member_id INT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_discord (discord_id),
    UNIQUE KEY unique_member (member_id),
    FOREIGN KEY (member_id) REFERENCES core_members(member_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Player Information
-- ==========================================
CREATE TABLE player_information (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    username VARCHAR(64) NOT NULL,
    total_level INT UNSIGNED NOT NULL DEFAULT 0,
    donator_role VARCHAR(64) DEFAULT NULL,
    ironman_mode TINYINT UNSIGNED NOT NULL DEFAULT 0,
    exp_mode TINYINT UNSIGNED NOT NULL DEFAULT 0,
    last_active TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user (user_id),
    INDEX (username),
    INDEX (ironman_mode),
    INDEX (exp_mode),
    FOREIGN KEY (user_id) REFERENCES core_members(member_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Punishments
-- ==========================================
CREATE TABLE actions (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    mod_name VARCHAR(64) NOT NULL,
    offender VARCHAR(64) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    expires BIGINT UNSIGNED DEFAULT NULL,
    reason TEXT NOT NULL,
    ip_address VARCHAR(64) DEFAULT NULL,
    mac_address VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (offender),
    INDEX (action_type),
    INDEX (expires)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Trade Logs
-- ==========================================
CREATE TABLE logs_trades (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user VARCHAR(64) NOT NULL,
    user_ip VARCHAR(64) DEFAULT NULL,
    given JSON NOT NULL,
    partner VARCHAR(64) NOT NULL,
    partner_ip VARCHAR(64) DEFAULT NULL,
    received JSON NOT NULL,
    world INT UNSIGNED NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (user),
    INDEX (partner),
    INDEX (world)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Antiknox Cache
-- ==========================================
CREATE TABLE antiknox_cache (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ipAddress VARCHAR(64) NOT NULL UNIQUE,
    messageDigest VARCHAR(191) NOT NULL,
    legitimate TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (ipAddress),
    INDEX (legitimate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- API Auth
-- ==========================================
CREATE TABLE api_auth (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(128) NOT NULL UNIQUE,
    ip VARCHAR(64) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL DEFAULT NULL,
    INDEX (token),
    INDEX (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Players Online
-- ==========================================
CREATE TABLE players_online (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    world VARCHAR(64) NOT NULL UNIQUE,
    online INT UNSIGNED NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX (world)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- IP Tables + ASN Blacklist
-- ==========================================
CREATE TABLE iptables_all (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    ipAddress VARCHAR(64) NOT NULL UNIQUE,
    valid TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX (ipAddress),
    INDEX (valid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE asn_blacklist (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    asn INT UNSIGNED NOT NULL,
    reason VARCHAR(191) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (asn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Hiscores
-- ==========================================
CREATE TABLE skill_hiscores (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    userid INT UNSIGNED NOT NULL,
    username VARCHAR(64) NOT NULL,
    mode TINYINT UNSIGNED NOT NULL DEFAULT 0,
    xp_mode TINYINT UNSIGNED NOT NULL DEFAULT 0,
    skill_id INT UNSIGNED NOT NULL,
    skill_name VARCHAR(64) NOT NULL,
    level INT UNSIGNED NOT NULL DEFAULT 1,
    experience BIGINT UNSIGNED NOT NULL DEFAULT 0,
    last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_skill (userid, mode, xp_mode, skill_id),
    INDEX (username),
    INDEX (skill_name),
    INDEX (level),
    INDEX (experience),
    INDEX (mode),
    INDEX (xp_mode),
    FOREIGN KEY (userid) REFERENCES core_members(member_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
