package com.newsapp.service;

import com.newsapp.entity.Subscription;
import com.newsapp.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订阅服务
 */
@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * 获取用户的所有订阅
     */
    public List<Subscription> getUserSubscriptions(Long userId) {
        log.debug("获取用户订阅列表: userId={}", userId);
        List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
        log.info("用户 {} 有 {} 个订阅", userId, subscriptions.size());
        return subscriptions;
    }

    /**
     * 订阅新闻类别
     */
    @Transactional
    public Subscription subscribe(Long userId, String category) {
        log.info("用户订阅请求: userId={}, category={}", userId, category);

        // 检查是否已订阅
        if (subscriptionRepository.existsByUserIdAndCategory(userId, category)) {
            log.warn("订阅失败: 用户 {} 已订阅类别 {}", userId, category);
            throw new RuntimeException("您已订阅该类别");
        }

        // 验证类别是否有效
        if (!isValidCategory(category)) {
            log.warn("订阅失败: 无效的新闻类别 {}", category);
            throw new RuntimeException("无效的新闻类别");
        }

        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setCategory(category);

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("订阅成功: userId={}, category={}", userId, category);

        return saved;
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void unsubscribe(Long userId, String category) {
        log.info("取消订阅请求: userId={}, category={}", userId, category);

        if (!subscriptionRepository.existsByUserIdAndCategory(userId, category)) {
            log.warn("取消订阅失败: 用户 {} 未订阅类别 {}", userId, category);
            throw new RuntimeException("您未订阅该类别");
        }

        subscriptionRepository.deleteByUserIdAndCategory(userId, category);
        log.info("取消订阅成功: userId={}, category={}", userId, category);
    }

    /**
     * 验证新闻类别是否有效
     */
    private boolean isValidCategory(String category) {
        // NewsAPI 支持的类别
        List<String> validCategories = List.of(
                "business", "entertainment", "general",
                "health", "science", "sports", "technology"
        );

        return validCategories.contains(category.toLowerCase());
    }
}
