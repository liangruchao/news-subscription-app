-- ============================================
-- RSS新闻源系统 - 数据库表结构
-- ============================================

-- 1. RSS源表
CREATE TABLE IF NOT EXISTS rss_feeds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT 'RSS源标题',
    url VARCHAR(500) NOT NULL UNIQUE COMMENT 'RSS feed URL',
    description TEXT COMMENT 'RSS源描述',
    category VARCHAR(100) COMMENT '分类（科技/财经/娱乐等）',
    language VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言',
    icon_url VARCHAR(500) COMMENT 'RSS源图标URL',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    last_fetched_at TIMESTAMP NULL COMMENT '最后抓取时间',
    last_fetched_status VARCHAR(50) COMMENT '最后抓取状态（success/failed）',
    last_fetched_error TEXT COMMENT '最后抓取错误信息',
    fetch_interval INT DEFAULT 30 COMMENT '抓取间隔（分钟）',
    article_count INT DEFAULT 0 COMMENT '文章总数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_category (category),
    INDEX idx_is_active (is_active),
    INDEX idx_last_fetched (last_fetched_at),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RSS源表';

-- 2. 文章表
CREATE TABLE IF NOT EXISTS articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feed_id BIGINT NOT NULL COMMENT '关联的RSS源ID',
    guid VARCHAR(500) NOT NULL COMMENT '文章唯一标识（RSS中的guid或link）',
    title VARCHAR(500) NOT NULL COMMENT '文章标题',
    link VARCHAR(1000) NOT NULL COMMENT '文章原文链接',
    author VARCHAR(200) COMMENT '作者',
    description TEXT COMMENT '文章摘要/描述',
    content LONGTEXT COMMENT '文章正文内容',
    pub_date TIMESTAMP NULL COMMENT '发布时间',
    category VARCHAR(100) COMMENT '分类',
    image_url VARCHAR(500) COMMENT '封面图片URL',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_guid (feed_id, guid(255)),
    INDEX idx_feed_id (feed_id),
    INDEX idx_pub_date (pub_date DESC),
    INDEX idx_category (category),
    FULLTEXT INDEX ft_title_content (title, content) WITH PARSER ngram,
    FULLTEXT INDEX ft_description (description) WITH PARSER ngram,

    CONSTRAINT fk_articles_feed FOREIGN KEY (feed_id)
        REFERENCES rss_feeds(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- 3. 用户RSS订阅表
CREATE TABLE IF NOT EXISTS user_rss_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    feed_id BIGINT NOT NULL COMMENT 'RSS源ID',
    custom_title VARCHAR(200) COMMENT '用户自定义标题',
    is_favorite BOOLEAN DEFAULT FALSE COMMENT '是否收藏该源',
    priority INT DEFAULT 0 COMMENT '优先级（用于排序）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '订阅时间',

    UNIQUE KEY uk_user_feed (user_id, feed_id),
    INDEX idx_user_id (user_id),
    INDEX idx_feed_id (feed_id),
    INDEX idx_is_favorite (is_favorite),

    CONSTRAINT fk_user_rss_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_rss_feed FOREIGN KEY (feed_id)
        REFERENCES rss_feeds(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户RSS订阅表';

-- 4. 用户收藏表
CREATE TABLE IF NOT EXISTS user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    notes TEXT COMMENT '用户笔记',
    tags VARCHAR(500) COMMENT '用户标签（逗号分隔）',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_user_article (user_id, article_id),
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_tags (tags(100)),

    CONSTRAINT fk_favorites_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_article FOREIGN KEY (article_id)
        REFERENCES articles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- 5. 文章分享表
CREATE TABLE IF NOT EXISTS article_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    share_code VARCHAR(32) NOT NULL UNIQUE COMMENT '分享码（MD5生成）',
    user_id BIGINT NOT NULL COMMENT '分享者ID',
    article_id BIGINT NOT NULL COMMENT '分享的文章ID',
    title VARCHAR(500) COMMENT '分享标题（可自定义）',
    description TEXT COMMENT '分享描述/评论',
    view_count INT DEFAULT 0 COMMENT '访问次数',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    expires_at TIMESTAMP NULL COMMENT '过期时间（NULL表示永不过期）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_share_code (share_code),
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id),
    INDEX idx_is_active (is_active),
    INDEX idx_expires_at (expires_at),

    CONSTRAINT fk_shares_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_article FOREIGN KEY (article_id)
        REFERENCES articles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章分享表';

-- 6. OPML导入历史表
CREATE TABLE IF NOT EXISTS opml_import_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    file_name VARCHAR(255) NOT NULL COMMENT 'OPML文件名',
    feed_count INT DEFAULT 0 COMMENT '导入的RSS源数量',
    success_count INT DEFAULT 0 COMMENT '成功导入数量',
    failed_count INT DEFAULT 0 COMMENT '失败数量',
    error_summary TEXT COMMENT '错误摘要',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间',

    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at DESC),

    CONSTRAINT fk_opml_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OPML导入历史表';

-- ============================================
-- 预设RSS源数据
-- ============================================

INSERT INTO rss_feeds (title, url, description, category, language) VALUES
('36Kr', 'https://36kr.com/feed', '36氪是中国领先的科技新媒体，报道最新互联网创业资讯', '科技', 'zh-CN'),
('虎嗅网', 'https://www.huxiu.com/rss/0.xml', '虎嗅网是一家个性化的商业资讯网站', '科技', 'zh-CN'),
('阮一峰博客', 'https://www.ruanyifeng.com/blog/atom.xml', '阮一峰的网络日志，记录技术、互联网、生活', '技术', 'zh-CN'),
('少数派', 'https://sspai.com/feed', '少数派致力于更好地运用数字产品或提高数字生活品质', '科技', 'zh-CN'),
('财新网', 'https://www.caixin.com/rss/rss_finance.xml', '财新网是财经新闻资讯提供商', '财经', 'zh-CN'),
('晚点LatePost', 'https://www.latepost.com/feed', '晚点LatePost提供深度的商业报道', '商业', 'zh-CN'),
('爱范儿', 'https://www.ifanr.com/feed', '爱范儿专注于时尚科技产品，导购资讯', '科技', 'zh-CN'),
('钛媒体', 'https://www.tmtpost.com/feed', '钛媒体是TMT领域科技媒体', '商业', 'zh-CN')
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;
