package com.newsapp.config;

import com.newsapp.service.RssFetchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * RSS抓取定时任务
 */
@Component
public class RssFetchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RssFetchScheduler.class);

    private final RssFetchService rssFetchService;

    public RssFetchScheduler(RssFetchService rssFetchService) {
        this.rssFetchService = rssFetchService;
    }

    /**
     * 定时抓取RSS源
     * 每30分钟执行一次（可通过配置文件修改cron表达式）
     */
    @Scheduled(cron = "${rss.fetch.cron:0 */30 * * * ?}")
    public void fetchRssFeedsScheduled() {
        logger.info("========== 定时RSS抓取任务开始 ==========");

        try {
            int articleCount = rssFetchService.fetchFeedsByInterval();
            logger.info("定时RSS抓取任务完成，新增文章数量: {}", articleCount);
        } catch (Exception e) {
            logger.error("定时RSS抓取任务执行失败: {}", e.getMessage(), e);
        }

        logger.info("========== 定时RSS抓取任务结束 ==========");
    }

    /**
     * 全量抓取所有RSS源
     * 每天凌晨2点执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void fetchAllRssFeedsDaily() {
        logger.info("========== 每日全量RSS抓取任务开始 ==========");

        try {
            int articleCount = rssFetchService.fetchAllActiveFeeds();
            logger.info("每日全量RSS抓取任务完成，新增文章数量: {}", articleCount);
        } catch (Exception e) {
            logger.error("每日全量RSS抓取任务执行失败: {}", e.getMessage(), e);
        }

        logger.info("========== 每日全量RSS抓取任务结束 ==========");
    }

    /**
     * 应用启动后延迟执行首次抓取
     * 延迟5分钟后执行，确保应用完全启动
     */
    @Scheduled(initialDelay = 300000, fixedDelay = Long.MAX_VALUE)
    public void initialFetch() {
        logger.info("========== 应用启动后首次RSS抓取任务开始 ==========");

        try {
            int articleCount = rssFetchService.fetchFeedsByInterval();
            logger.info("应用启动后首次RSS抓取任务完成，新增文章数量: {}", articleCount);
        } catch (Exception e) {
            logger.error("应用启动后首次RSS抓取任务执行失败: {}", e.getMessage(), e);
        }

        logger.info("========== 应用启动后首次RSS抓取任务结束 ==========");
    }
}
