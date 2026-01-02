package com.newsapp.service;

import com.newsapp.dto.UserStatisticsResponse;
import com.newsapp.entity.LoginHistory;
import com.newsapp.entity.User;
import com.newsapp.repository.LoginHistoryRepository;
import com.newsapp.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 统计服务
 */
@Service
public class StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public StatisticsService(SubscriptionRepository subscriptionRepository,
                            LoginHistoryRepository loginHistoryRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    /**
     * 获取用户统计数据
     */
    public UserStatisticsResponse getUserStatistics(User user) {
        log.info("获取用户统计数据: userId={}", user.getId());

        UserStatisticsResponse stats = new UserStatisticsResponse();

        // 阅读统计 (暂时返回模拟数据，后续可以根据实际阅读记录表实现)
        stats.setTotalArticlesRead(0L);
        stats.setReadingTimeMinutes(0L);

        // 订阅统计
        Long subscriptionCount = subscriptionRepository.countByUserId(user.getId());
        stats.setTotalSubscriptions(subscriptionCount);
        stats.setActiveCategories(subscriptionCount); // 简化处理，假设每个订阅对应一个分类

        // 活跃度统计
        LocalDateTime createdAt = user.getCreatedAt();
        long daysSinceJoined = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        stats.setDaysSinceJoined(daysSinceJoined);

        // 最近登录时间
        List<LoginHistory> recentLogins = loginHistoryRepository
                .findTop10ByUserIdOrderByLoginTimeDesc(user.getId());
        if (!recentLogins.isEmpty()) {
            stats.setLastLoginTime(recentLogins.get(0).getLoginTime());
            stats.setLoginCount((long) recentLogins.size());
        } else {
            stats.setLoginCount(0L);
        }

        return stats;
    }
}
