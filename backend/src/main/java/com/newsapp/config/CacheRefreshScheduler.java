package com.newsapp.config;

import com.newsapp.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 缓存自动刷新定时任务
 * 定期后台刷新新闻缓存，确保用户无感知
 */
@Component
@EnableScheduling
public class CacheRefreshScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshScheduler.class);

    @Autowired
    private NewsService newsService;

    @Value("${cache.auto-refresh:true}")
    private boolean autoRefreshEnabled;

    /**
     * 应用启动后延迟预加载缓存
     * 延迟 5 秒执行，确保应用完全启动
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void preloadOnStartup() {
        logger.info("========== 应用启动预加载开始 ==========");
        try {
            newsService.preloadAllCategories();
        } catch (Exception e) {
            logger.error("预加载失败: {}", e.getMessage());
        }
    }

    /**
     * 自动刷新缓存
     * 每 8 分钟执行一次（fixedRate = 480000ms）
     * 使用 fixedRate 确保定时执行，不受任务执行时间影响
     */
    @Scheduled(initialDelay = 480000, fixedRate = 480000)
    public void autoRefreshCache() {
        if (!autoRefreshEnabled) {
            logger.debug("自动刷新未启用");
            return;
        }

        logger.info("========== 执行定时自动刷新 ==========");
        try {
            newsService.autoRefreshAllCache();
        } catch (Exception e) {
            logger.error("自动刷新失败: {}", e.getMessage());
        }
    }
}
