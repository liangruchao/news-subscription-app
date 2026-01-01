-- News Subscription Application 数据库初始化脚本
-- 版本: 1.0
-- 日期: 2026-01-01
-- 说明: 创建 users 和 subscriptions 表

-- ============================================================
-- 数据库和字符集设置
-- ============================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================================
-- 删除已存在的表（开发环境用，生产环境慎用）
-- ============================================================

DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS users;

-- ============================================================
-- 创建用户表
-- ============================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 创建订阅表
-- ============================================================

CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_user_category (user_id, category),
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),

    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- 验证表结构
-- ============================================================

SHOW CREATE TABLE users;
SHOW CREATE TABLE subscriptions;

-- ============================================================
-- 完成
-- ============================================================

SELECT 'Database initialization completed!' AS Status;
