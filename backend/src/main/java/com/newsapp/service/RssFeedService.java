package com.newsapp.service;

import com.newsapp.entity.RssFeed;
import com.newsapp.repository.RssFeedRepository;
import org.apache.commons.lang3.StringUtils;
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
 * RSS源管理服务
 */
@Service
public class RssFeedService {

    private static final Logger logger = LoggerFactory.getLogger(RssFeedService.class);

    private final RssFeedRepository rssFeedRepository;
    private final RssFetchService rssFetchService;

    public RssFeedService(RssFeedRepository rssFeedRepository, RssFetchService rssFetchService) {
        this.rssFeedRepository = rssFeedRepository;
        this.rssFetchService = rssFetchService;
    }

    /**
     * 获取所有RSS源（分页）
     */
    @Cacheable(value = "rssFeeds", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<RssFeed> getAllFeeds(Pageable pageable) {
        return rssFeedRepository.findAll(pageable);
    }

    /**
     * 获取所有激活的RSS源
     */
    // @Cacheable(value = "activeRssFeeds")  // 暂时禁用缓存
    public List<RssFeed> getActiveFeeds() {
        return rssFeedRepository.findActiveFeeds();
    }

    /**
     * 根据ID获取RSS源
     */
    @Cacheable(value = "rssFeed", key = "#id")
    public Optional<RssFeed> getFeedById(Long id) {
        return rssFeedRepository.findById(id);
    }

    /**
     * 根据URL获取RSS源
     */
    public Optional<RssFeed> getFeedByUrl(String url) {
        return rssFeedRepository.findByUrl(url);
    }

    /**
     * 根据分类获取RSS源
     */
    @Cacheable(value = "rssFeedsByCategory", key = "#category")
    public List<RssFeed> getFeedsByCategory(String category) {
        return rssFeedRepository.findByCategoryAndIsActive(category, true);
    }

    /**
     * 搜索RSS源（标题）
     */
    public List<RssFeed> searchFeeds(String keyword) {
        return rssFeedRepository.searchByTitle(keyword);
    }

    /**
     * 获取所有分类
     */
    @Cacheable(value = "rssCategories")
    public List<String> getAllCategories() {
        return rssFeedRepository.findAllCategories();
    }

    /**
     * 添加RSS源
     */
    @Transactional
    @CacheEvict(value = {"rssFeeds", "activeRssFeeds", "rssFeedsByCategory", "rssCategories"}, allEntries = true)
    public RssFeed addFeed(RssFeed feed) {
        // 检查URL是否已存在
        if (rssFeedRepository.existsByUrl(feed.getUrl())) {
            throw new IllegalArgumentException("RSS源URL已存在: " + feed.getUrl());
        }

        // 测试URL是否有效，并获取RSS信息
        if (!rssFetchService.testRssUrl(feed.getUrl())) {
            throw new IllegalArgumentException("RSS源URL无效或无法访问: " + feed.getUrl());
        }

        // 如果没有提供标题，尝试从RSS获取
        if (StringUtils.isBlank(feed.getTitle())) {
            String title = rssFetchService.fetchTitleFromUrl(feed.getUrl());
            if (StringUtils.isNotBlank(title)) {
                feed.setTitle(title);
            } else {
                // 使用URL作为默认标题
                feed.setTitle(feed.getUrl());
            }
        }

        // 如果没有提供分类，设置为默认值
        if (StringUtils.isBlank(feed.getCategory())) {
            feed.setCategory("未分类");
        }

        // 如果没有提供语言，设置为默认值
        if (StringUtils.isBlank(feed.getLanguage())) {
            feed.setLanguage("zh-CN");
        }

        RssFeed savedFeed = rssFeedRepository.save(feed);
        logger.info("添加RSS源成功: {} ({})", savedFeed.getTitle(), savedFeed.getUrl());

        // 立即抓取一次
        try {
            rssFetchService.fetchRssFeed(savedFeed.getId());
        } catch (Exception e) {
            logger.warn("添加RSS源后抓取失败: {} - {}", savedFeed.getTitle(), e.getMessage());
        }

        return savedFeed;
    }

    /**
     * 更新RSS源
     */
    @Transactional
    @CacheEvict(value = {"rssFeeds", "activeRssFeeds", "rssFeed", "rssFeedsByCategory"}, allEntries = true)
    public RssFeed updateFeed(Long id, RssFeed feedDetails) {
        RssFeed feed = rssFeedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RSS源不存在: id=" + id));

        // 更新字段
        if (feedDetails.getTitle() != null) {
            feed.setTitle(feedDetails.getTitle());
        }
        if (feedDetails.getDescription() != null) {
            feed.setDescription(feedDetails.getDescription());
        }
        if (feedDetails.getCategory() != null) {
            feed.setCategory(feedDetails.getCategory());
        }
        if (feedDetails.getLanguage() != null) {
            feed.setLanguage(feedDetails.getLanguage());
        }
        if (feedDetails.getIconUrl() != null) {
            feed.setIconUrl(feedDetails.getIconUrl());
        }
        if (feedDetails.getIsActive() != null) {
            feed.setIsActive(feedDetails.getIsActive());
        }
        if (feedDetails.getFetchInterval() != null) {
            feed.setFetchInterval(feedDetails.getFetchInterval());
        }

        // 如果更新了URL，需要测试并重新抓取
        if (feedDetails.getUrl() != null && !feedDetails.getUrl().equals(feed.getUrl())) {
            if (rssFeedRepository.existsByUrl(feedDetails.getUrl())) {
                throw new IllegalArgumentException("RSS源URL已被使用: " + feedDetails.getUrl());
            }
            if (!rssFetchService.testRssUrl(feedDetails.getUrl())) {
                throw new IllegalArgumentException("RSS源URL无效: " + feedDetails.getUrl());
            }
            feed.setUrl(feedDetails.getUrl());
            feed.setLastFetchedAt(null); // 重置抓取时间
        }

        RssFeed updatedFeed = rssFeedRepository.save(feed);
        logger.info("更新RSS源成功: {} (id={})", updatedFeed.getTitle(), id);

        return updatedFeed;
    }

    /**
     * 删除RSS源
     */
    @Transactional
    @CacheEvict(value = {"rssFeeds", "activeRssFeeds", "rssFeed", "rssFeedsByCategory", "rssCategories"}, allEntries = true)
    public void deleteFeed(Long id) {
        if (!rssFeedRepository.existsById(id)) {
            throw new IllegalArgumentException("RSS源不存在: id=" + id);
        }

        rssFeedRepository.deleteById(id);
        logger.info("删除RSS源成功: id={}", id);
    }

    /**
     * 激活/禁用RSS源
     */
    @Transactional
    @CacheEvict(value = {"rssFeeds", "activeRssFeeds"}, allEntries = true)
    public void toggleFeedActive(Long id, boolean isActive) {
        RssFeed feed = rssFeedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RSS源不存在: id=" + id));

        feed.setIsActive(isActive);
        rssFeedRepository.save(feed);

        logger.info("{}RSS源: {} (id={})", isActive ? "激活" : "禁用", feed.getTitle(), id);
    }

    /**
     * 手动触发RSS源抓取
     */
    @Transactional
    public int fetchFeed(Long id) {
        return rssFetchService.fetchRssFeed(id);
    }

    /**
     * 手动触发所有激活的RSS源抓取
     */
    @Transactional
    public int fetchAllFeeds() {
        return rssFetchService.fetchAllActiveFeeds();
    }

    /**
     * 清除所有缓存
     */
    @CacheEvict(value = {"rssFeeds", "activeRssFeeds", "rssFeed", "rssFeedsByCategory", "rssCategories"}, allEntries = true)
    public void clearCache() {
        logger.info("清除RSS源缓存");
    }
}
