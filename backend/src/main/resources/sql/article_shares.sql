-- 文章分享表
CREATE TABLE IF NOT EXISTS article_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    share_code VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    article_id BIGINT NOT NULL,
    title VARCHAR(500),
    description TEXT,
    view_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_share_code (share_code),
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_active (is_active),

    CONSTRAINT fk_shares_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_article FOREIGN KEY (article_id)
        REFERENCES articles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
