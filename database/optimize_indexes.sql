-- 数据库索引优化脚本
-- 日期: 2026-01-01
-- 说明: 当前索引已覆盖所有查询，此脚本为未来扩展准备

-- ============================================================
-- 当前索引状态
-- ============================================================
-- users 表:
--   - PRIMARY KEY (id)
--   - UNIQUE INDEX (email)
--   - UNIQUE INDEX (username)
--
-- subscriptions 表:
--   - PRIMARY KEY (id)
--   - UNIQUE INDEX (user_id, category)

-- ============================================================
-- 1. 查看当前索引使用情况
-- ============================================================

-- 查看表结构和索引
SHOW INDEX FROM users;
SHOW INDEX FROM subscriptions;

-- 查看表统计信息
SHOW TABLE STATUS FROM news_app;

-- ============================================================
-- 2. 分析查询性能
-- ============================================================

-- 使用 EXPLAIN 分析常用查询
EXPLAIN SELECT * FROM users WHERE username = 'testuser';
EXPLAIN SELECT * FROM users WHERE email = 'test@example.com';
EXPLAIN SELECT * FROM subscriptions WHERE user_id = 1;
EXPLAIN SELECT * FROM subscriptions WHERE user_id = 1 AND category = 'technology';

-- ============================================================
-- 3. 未来扩展优化 (当前不需要执行)
-- ============================================================

-- 3.1 如果需要按时间排序的查询，可以添加时间索引
-- ALTER TABLE users ADD INDEX idx_created_at (created_at);
-- ALTER TABLE subscriptions ADD INDEX idx_created_at (created_at);

-- 3.2 如果需要统计活跃用户，可以添加复合索引
-- ALTER TABLE subscriptions ADD INDEX idx_user_category_created (user_id, category, created_at);

-- 3.3 如果需要按用户查找并按订阅数量排序
-- ALTER TABLE subscriptions ADD INDEX idx_user_count (user_id, category);

-- ============================================================
-- 4. 数据库配置优化建议 (my.cnf)
-- ============================================================

-- 以下配置建议添加到 MySQL 配置文件中:

-- [mysqld]
-- innodb_buffer_pool_size = 1G  # 设置为物理内存的 50-70%
-- innodb_log_file_size = 256M
-- innodb_flush_log_at_trx_commit = 2  # 提高写入性能
-- innodb_flush_method = O_DIRECT
-- max_connections = 200
-- query_cache_size = 128M  # MySQL 5.7 及以下
-- query_cache_type = 1

-- ============================================================
-- 5. 定期维护脚本
-- ============================================================

-- 分析表以优化查询计划
ANALYZE TABLE users;
ANALYZE TABLE subscriptions;

-- 优化表（会重建表，谨慎使用）
-- OPTIMIZE TABLE users;
-- OPTIMIZE TABLE subscriptions;

-- ============================================================
-- 6. 监控慢查询
-- ============================================================

-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;  # 记录执行时间超过 1 秒的查询
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 查看慢查询日志配置
SHOW VARIABLES LIKE 'slow_query_log%';
SHOW VARIABLES LIKE 'long_query_time';

-- ============================================================
-- 7. 索引使用率统计
-- ============================================================

-- 查看 InnoDB 缓冲池状态
SHOW ENGINE INNODB STATUS\G

-- 查看表统计信息
SELECT
    TABLE_NAME,
    TABLE_ROWS,
    DATA_LENGTH / 1024 / 1024 AS 'Data Size (MB)',
    INDEX_LENGTH / 1024 / 1024 AS 'Index Size (MB)',
    (DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024 AS 'Total Size (MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'news_app'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;
