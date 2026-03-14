package com.newsapp.config;

import com.newsapp.service.ArticleShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 分享清理定时任务
 */
@Component
public class ShareCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ShareCleanupScheduler.class);

    private final ArticleShareService shareService;

    public ShareCleanupScheduler(ArticleShareService shareService) {
        this.shareService = shareService;
    }

    /**
     * 每天凌晨2点清理过期分享
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredShares() {
        logger.info("开始清理过期的文章分享链接");
        try {
            int count = shareService.cleanExpiredShares();
            logger.info("清理过期分享完成: 处理 {} 条", count);
        } catch (Exception e) {
            logger.error("清理过期分享失败", e);
        }
    }
}
