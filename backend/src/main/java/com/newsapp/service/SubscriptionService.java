package com.newsapp.service;

import com.newsapp.entity.Subscription;
import com.newsapp.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订阅服务
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * 获取用户的所有订阅
     */
    public List<Subscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    /**
     * 订阅新闻类别
     */
    @Transactional
    public Subscription subscribe(Long userId, String category) {
        // 检查是否已订阅
        if (subscriptionRepository.existsByUserIdAndCategory(userId, category)) {
            throw new RuntimeException("您已订阅该类别");
        }

        // 验证类别是否有效
        if (!isValidCategory(category)) {
            throw new RuntimeException("无效的新闻类别");
        }

        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setCategory(category);

        return subscriptionRepository.save(subscription);
    }

    /**
     * 取消订阅
     */
    @Transactional
    public void unsubscribe(Long userId, String category) {
        if (!subscriptionRepository.existsByUserIdAndCategory(userId, category)) {
            throw new RuntimeException("您未订阅该类别");
        }

        subscriptionRepository.deleteByUserIdAndCategory(userId, category);
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
