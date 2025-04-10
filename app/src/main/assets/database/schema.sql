-- Database Schema for Chat Application
-- Created: 2025-03-27 19:58:21
-- Author: Maltan-26

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    profile_image_url TEXT,
    status VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP,
    is_online BOOLEAN DEFAULT FALSE
);

-- Chat Rooms Table
CREATE TABLE IF NOT EXISTS chat_rooms (
    room_id VARCHAR(100) PRIMARY KEY,
    last_message TEXT,
    last_message_time BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_group BOOLEAN DEFAULT FALSE
);

-- Room Participants Table
CREATE TABLE IF NOT EXISTS room_participants (
    room_id VARCHAR(100),
    user_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_message_id BIGINT,
    PRIMARY KEY (room_id, user_id),
    FOREIGN KEY (room_id) REFERENCES chat_rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Messages Table
CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id VARCHAR(100) NOT NULL,
    sender_uid BIGINT NOT NULL,
    message_text TEXT NOT NULL,
    timestamp BIGINT NOT NULL,
    time_string VARCHAR(30) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_delivered BOOLEAN DEFAULT FALSE,
    message_type VARCHAR(20) DEFAULT 'text',
    media_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_id) REFERENCES chat_rooms(room_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_uid) REFERENCES users(user_id) ON DELETE CASCADE
);

-- OTP Verification Table
CREATE TABLE IF NOT EXISTS otp_verification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number VARCHAR(15) NOT NULL,
    otp VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_verified BOOLEAN DEFAULT FALSE,
    attempt_count INT DEFAULT 0,
    UNIQUE (phone_number)
);

-- User Status Updates
CREATE TABLE IF NOT EXISTS user_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    status_text TEXT,
    media_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- User Blocking Table
CREATE TABLE IF NOT EXISTS user_blocks (
    blocker_id BIGINT,
    blocked_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (blocker_id, blocked_id),
    FOREIGN KEY (blocker_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- SMS Attempts Tracking
CREATE TABLE IF NOT EXISTS sms_attempts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone_number VARCHAR(15) NOT NULL,
    success BOOLEAN NOT NULL,
    attempt_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_messages_room_timestamp ON messages(room_id, timestamp);
CREATE INDEX idx_messages_sender ON messages(sender_uid);
CREATE INDEX idx_room_participants_user ON room_participants(user_id);
CREATE INDEX idx_otp_phone ON otp_verification(phone_number);
CREATE INDEX idx_user_phone ON users(phone_number);
CREATE INDEX idx_sms_attempts_phone ON sms_attempts(phone_number);
CREATE INDEX idx_chat_rooms_updated ON chat_rooms(updated_at);