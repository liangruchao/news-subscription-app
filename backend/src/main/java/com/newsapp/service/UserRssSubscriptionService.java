package com.newsapp.service;

import com.newsapp.entity.RssFeed;
import com.newsapp.entity.UserRssSubscription;
import com.newsapp.repository.RssFeedRepository;
import com.newsapp.repository.UserRssSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户RSS订阅服务
 */
@Service
public class UserRssSubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(UserRssSubscriptionService.class);

    private final UserRssSubscriptionRepository subscriptionRepository;
    private final RssFeedRepository rssFeedRepository;

    public UserRssSubscriptionService(UserRssSubscriptionRepository subscriptionRepository,
                                       RssFeedRepository rssFeedRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.rssFeedRepository = rssFeedRepository;
    }

    /**
     * 获取用户的所有订阅（包含RSS源信息）
     */
    // @Cacheable(value = "userSubscriptions", key = "#userId")  // 暂时禁用缓存以解决 LocalDateTime 序列化问题
    public List<RssFeed> getUserSubscriptionsWithFeeds(Long userId) {
        logger.info("获取用户订阅列表: userId={}", userId);

        List<UserRssSubscription> subscriptions = subscriptionRepository
                .findByUserIdOrderByPriorityAscCreatedAtDesc(userId);

        return subscriptions.stream()
                .map(sub -> {
                    Optional<RssFeed> feedOpt = rssFeedRepository.findById(sub.getFeedId());
                    if (feedOpt.isPresent()) {
                        RssFeed feed = feedOpt.get();
                        // 添加订阅相关信息到feed
                        return feed;
                    }
                    return null;
                })
                .filter(feed -> feed != null)
                .toList();
    }

    /**
     * 获取用户的所有订阅实体
     */
    public List<UserRssSubscription> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdOrderByPriorityAscCreatedAtDesc(userId);
    }

    /**
     * 获取用户收藏的RSS源
     */
    // @Cacheable(value = "userFavoriteFeeds", key = "#userId")  // 暂时禁用缓存以解决 LocalDateTime 序列化问题
    public List<RssFeed> getUserFavoriteFeeds(Long userId) {
        logger.info("获取用户收藏的RSS源: userId={}", userId);

        List<UserRssSubscription> subscriptions = subscriptionRepository
                .findByUserIdAndIsFavoriteTrueOrderByPriorityAscCreatedAtDesc(userId);

        return subscriptions.stream()
                .map(sub -> rssFeedRepository.findById(sub.getFeedId()).orElse(null))
                .filter(feed -> feed != null)
                .toList();
    }

    /**
     * 检查用户是否订阅了指定RSS源
     */
    public boolean isSubscribed(Long userId, Long feedId) {
        return subscriptionRepository.existsByUserIdAndFeedId(userId, feedId);
    }

    /**
     * 订阅RSS源
     */
    @Transactional
    @CacheEvict(value = {"userSubscriptions", "userFavoriteFeeds"}, key = "#userId")
    public UserRssSubscription subscribe(Long userId, Long feedId, String customTitle) {
        logger.info("用户订阅RSS源: userId={}, feedId={}", userId, feedId);

        // 检查RSS源是否存在
        if (!rssFeedRepository.existsById(feedId)) {
            throw new IllegalArgumentException("RSS源不存在: id=" + feedId);
        }

        // 检查是否已订阅
        if (subscriptionRepository.existsByUserIdAndFeedId(userId, feedId)) {
            throw new IllegalArgumentException("已经订阅了该RSS源");
        }

        UserRssSubscription subscription = new UserRssSubscription();
        subscription.setUserId(userId);
        subscription.setFeedId(feedId);
        subscription.setCustomTitle(customTitle);
        subscription.setIsFavorite(false);
        subscription.setPriority(0);

        UserRssSubscription saved = subscriptionRepository.save(subscription);
        logger.info("订阅成功: id={}", saved.getId());

        return saved;
    }

    /**
     * 取消订阅
     */
    @Transactional
    @CacheEvict(value = {"userSubscriptions", "userFavoriteFeeds"}, key = "#userId")
    public void unsubscribe(Long userId, Long feedId) {
        logger.info("用户取消订阅: userId={}, feedId={}", userId, feedId);

        if (!subscriptionRepository.existsByUserIdAndFeedId(userId, feedId)) {
            throw new IllegalArgumentException("未订阅该RSS源");
        }

        subscriptionRepository.deleteByUserIdAndFeedId(userId, feedId);
        logger.info("取消订阅成功: userId={}, feedId={}", userId, feedId);
    }

    /**
     * 切换收藏状态
     */
    @Transactional
    @CacheEvict(value = {"userSubscriptions", "userFavoriteFeeds"}, key = "#userId")
    public void toggleFavorite(Long userId, Long feedId) {
        logger.info("切换收藏状态: userId={}, feedId={}", userId, feedId);

        UserRssSubscription subscription = subscriptionRepository
                .findByUserIdAndFeedId(userId, feedId)
                .orElseThrow(() -> new IllegalArgumentException("未订阅该RSS源"));

        subscription.setIsFavorite(!subscription.getIsFavorite());
        subscriptionRepository.save(subscription);

        logger.info("收藏状态更新: isFavorite={}", subscription.getIsFavorite());
    }

    /**
     * 更新订阅优先级
     */
    @Transactional
    @CacheEvict(value = {"userSubscriptions", "userFavoriteFeeds"}, key = "#userId")
    public void updatePriority(Long userId, Long feedId, Integer priority) {
        logger.info("更新订阅优先级: userId={}, feedId={}, priority={}", userId, feedId, priority);

        UserRssSubscription subscription = subscriptionRepository
                .findByUserIdAndFeedId(userId, feedId)
                .orElseThrow(() -> new IllegalArgumentException("未订阅该RSS源"));

        subscription.setPriority(priority);
        subscriptionRepository.save(subscription);

        logger.info("优先级更新成功");
    }

    /**
     * 更新自定义标题
     */
    @Transactional
    @CacheEvict(value = "userSubscriptions", key = "#userId")
    public void updateCustomTitle(Long userId, Long feedId, String customTitle) {
        logger.info("更新自定义标题: userId={}, feedId={}, title={}", userId, feedId, customTitle);

        UserRssSubscription subscription = subscriptionRepository
                .findByUserIdAndFeedId(userId, feedId)
                .orElseThrow(() -> new IllegalArgumentException("未订阅该RSS源"));

        subscription.setCustomTitle(customTitle);
        subscriptionRepository.save(subscription);

        logger.info("自定义标题更新成功");
    }

    /**
     * 批量订阅
     */
    @Transactional
    @CacheEvict(value = {"userSubscriptions", "userFavoriteFeeds"}, allEntries = true)
    public int batchSubscribe(Long userId, List<Long> feedIds) {
        logger.info("批量订阅: userId={}, count={}", userId, feedIds.size());

        int successCount = 0;
        for (Long feedId : feedIds) {
            try {
                if (!subscriptionRepository.existsByUserIdAndFeedId(userId, feedId)) {
                    UserRssSubscription subscription = new UserRssSubscription();
                    subscription.setUserId(userId);
                    subscription.setFeedId(feedId);
                    subscription.setIsFavorite(false);
                    subscription.setPriority(0);
                    subscriptionRepository.save(subscription);
                    successCount++;
                }
            } catch (Exception e) {
                logger.warn("订阅失败: feedId={}, error={}", feedId, e.getMessage());
            }
        }

        logger.info("批量订阅完成: 成功 {}/{}", successCount, feedIds.size());
        return successCount;
    }

    /**
     * 获取用户订阅数量
     */
    public long getSubscriptionCount(Long userId) {
        return subscriptionRepository.countByUserId(userId);
    }

    /**
     * 清除缓存
     */
    @CacheEvict(value = {"userSubscriptions", "userFavoriteFeeds"}, allEntries = true)
    public void clearCache() {
        logger.info("清除订阅缓存");
    }
}
