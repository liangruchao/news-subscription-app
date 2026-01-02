package com.newsapp.service;

import com.newsapp.dto.NewsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 负责新闻数据的缓存读写和管理
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${cache.ttl:600000}")
    private long cacheTtl;

    @Value("${cache.enabled:true}")
    private boolean cacheEnabled;

    // 缓存统计
    private long cacheHits = 0;
    private long cacheMisses = 0;
    private long apiCalls = 0;

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成缓存 Key
     * 格式: news:{category}
     */
    public String generateKey(String category) {
        return "news:" + category.toLowerCase();
    }

    /**
     * 从缓存获取新闻列表
     */
    @SuppressWarnings("unchecked")
    public List<NewsDto> getFromCache(String category) {
        if (!cacheEnabled) {
            logger.debug("缓存未启用");
            return null;
        }

        String key = generateKey(category);

        try {
            List<NewsDto> cachedNews = (List<NewsDto>) redisTemplate.opsForValue().get(key);

            if (cachedNews != null) {
                cacheHits++;
                logger.info("缓存命中: {}, 总命中次数: {}", key, cacheHits);
                return cachedNews;
            } else {
                cacheMisses++;
                logger.debug("缓存未命中: {}, 总未命中次数: {}", key, cacheMisses);
                return null;
            }
        } catch (Exception e) {
            logger.error("从缓存读取失败: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * 将新闻列表保存到缓存
     */
    public void saveToCache(String category, List<NewsDto> newsList) {
        if (!cacheEnabled) {
            logger.debug("缓存未启用");
            return;
        }

        String key = generateKey(category);

        try {
            redisTemplate.opsForValue().set(key, newsList, cacheTtl, TimeUnit.MILLISECONDS);
            logger.info("已保存到缓存: {}, TTL: {}ms, 新闻数量: {}", key, cacheTtl, newsList.size());
        } catch (Exception e) {
            logger.error("保存到缓存失败: {}, 错误: {}", key, e.getMessage());
        }
    }

    /**
     * 刷新指定类别的缓存
     */
    public void refreshCache(String category) {
        String key = generateKey(category);
        try {
            redisTemplate.delete(key);
            logger.info("已删除缓存: {}", key);
        } catch (Exception e) {
            logger.error("删除缓存失败: {}, 错误: {}", key, e.getMessage());
        }
    }

    /**
     * 检查缓存是否存在
     */
    public boolean existsInCache(String category) {
        if (!cacheEnabled) {
            return false;
        }

        String key = generateKey(category);
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            logger.error("检查缓存存在性失败: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * 获取缓存剩余时间（秒）
     */
    public long getCacheTtl(String category) {
        String key = generateKey(category);
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            logger.error("获取缓存TTL失败: {}, 错误: {}", key, e.getMessage());
            return -1;
        }
    }

    /**
     * 清空所有新闻缓存
     */
    public void clearAllCache() {
        try {
            // 删除所有 news: 前缀的键
            redisTemplate.delete(redisTemplate.keys("news:*"));
            logger.info("已清空所有新闻缓存");
        } catch (Exception e) {
            logger.error("清空缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 检查 Redis 连接状态
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            logger.error("Redis 连接失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 缓存统计方法
     */
    public long getCacheHits() {
        return cacheHits;
    }

    public long getCacheMisses() {
        return cacheMisses;
    }

    public long getApiCalls() {
        return apiCalls;
    }

    public void incrementApiCalls() {
        this.apiCalls++;
    }

    public double getCacheHitRate() {
        long total = cacheHits + cacheMisses;
        return total > 0 ? (double) cacheHits / total * 100 : 0;
    }

    /**
     * 重置统计数据
     */
    public void resetStatistics() {
        cacheHits = 0;
        cacheMisses = 0;
        apiCalls = 0;
        logger.info("缓存统计数据已重置");
    }

    /**
     * 获取缓存统计信息
     */
    public String getStatistics() {
        return String.format(
            "缓存命中: %d, 未命中: %d, API调用: %d, 命中率: %.2f%%",
            cacheHits, cacheMisses, apiCalls, getCacheHitRate()
        );
    }
}
