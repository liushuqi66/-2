-- Smart Interview Platform DDL
CREATE DATABASE IF NOT EXISTS smart_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_interview;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL COMMENT 'BCrypt',
    `real_name` VARCHAR(50), `email` VARCHAR(100), `phone` VARCHAR(20),
    `role` VARCHAR(20) DEFAULT 'candidate',
    `avatar_url` VARCHAR(500),
    `status` TINYINT DEFAULT 1 COMMENT '1正常/0禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_username` (`username`), KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `resume` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `education` VARCHAR(50), `school` VARCHAR(100), `major` VARCHAR(100),
    `experience_years` INT DEFAULT 0,
    `skills` TEXT, `summary` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';

CREATE TABLE IF NOT EXISTS `question` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `reference_answer` TEXT,
    `difficulty` TINYINT NOT NULL DEFAULT 2 COMMENT '1简单/2中等/3困难',
    `tags` VARCHAR(500), `knowledge_points` VARCHAR(500),
    `type` VARCHAR(20) DEFAULT 'open' COMMENT 'open/choice/code',
    `status` TINYINT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_difficulty` (`difficulty`), KEY `idx_tags` (`tags`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题库表';

CREATE TABLE IF NOT EXISTS `interview_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `position` VARCHAR(100) NOT NULL,
    `difficulty_level` TINYINT NOT NULL DEFAULT 2,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待开始/1进行中/2已完成/3已取消',
    `score` DECIMAL(5,2), `paper_id` VARCHAR(50),
    `start_time` DATETIME, `end_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`), KEY `idx_status` (`status`),
    KEY `idx_user_status` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试记录表';

CREATE TABLE IF NOT EXISTS `interview_answer` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `interview_id` BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `answer` TEXT, `score` DECIMAL(5,2), `feedback` TEXT,
    `question_order` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_interview_id` (`interview_id`),
    UNIQUE KEY `uk_interview_question` (`interview_id`,`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题记录表';
