package com.newsapp.service;

import com.newsapp.entity.Article;
import com.newsapp.entity.RssFeed;
import com.newsapp.repository.ArticleRepository;
import com.newsapp.repository.RssFeedRepository;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * RSS抓取服务 - 核心功能
 */
@Service
public class RssFetchService {

    private static final Logger logger = LoggerFactory.getLogger(RssFetchService.class);

    @Value("${rss.fetch.timeout:10000}")
    private int fetchTimeout;

    @Value("${rss.fetch.retry.max:3}")
    private int maxRetry;

    private final RssFeedRepository rssFeedRepository;
    private final ArticleRepository articleRepository;

    public RssFetchService(RssFeedRepository rssFeedRepository, ArticleRepository articleRepository) {
        this.rssFeedRepository = rssFeedRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * 抓取单个RSS源
     * @param feedId RSS源ID
     * @return 抓取到的文章数量
     */
    @Transactional
    public int fetchRssFeed(Long feedId) {
        Optional<RssFeed> feedOpt = rssFeedRepository.findById(feedId);
        if (feedOpt.isEmpty()) {
            logger.warn("RSS源不存在: id={}", feedId);
            return 0;
        }

        RssFeed feed = feedOpt.get();
        return fetchRssFeed(feed);
    }

    /**
     * 抓取单个RSS源
     * @param feed RSS源实体
     * @return 抓取到的文章数量
     */
    @Transactional
    public int fetchRssFeed(RssFeed feed) {
        if (!feed.getIsActive()) {
            logger.debug("RSS源已禁用，跳过抓取: {}", feed.getTitle());
            return 0;
        }

        int retryCount = 0;
        int newArticleCount = 0;

        while (retryCount < maxRetry) {
            try {
                logger.info("开始抓取RSS源: {} ({})", feed.getTitle(), feed.getUrl());

                // 使用Rome库解析RSS
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed syndFeed;
                try (XmlReader reader = new XmlReader(new URL(feed.getUrl()))) {
                    syndFeed = input.build(reader);
                }

                // 更新RSS源信息
                if (StringUtils.isNotBlank(syndFeed.getTitle()) && !feed.getTitle().equals(syndFeed.getTitle())) {
                    feed.setTitle(syndFeed.getTitle());
                }
                if (StringUtils.isNotBlank(syndFeed.getDescription()) && StringUtils.isBlank(feed.getDescription())) {
                    feed.setDescription(syndFeed.getDescription());
                }

                // 解析文章列表
                List<SyndEntry> entries = syndFeed.getEntries();
                logger.debug("RSS源 {} 包含 {} 篇文章", feed.getTitle(), entries.size());

                for (SyndEntry entry : entries) {
                    try {
                        // 检查文章是否已存在（去重）
                        String guid = getEntryGuid(entry);
                        if (articleRepository.existsByFeedIdAndGuid(feed.getId(), guid)) {
                            logger.debug("文章已存在，跳过: {}", entry.getTitle());
                            continue;
                        }

                        // 创建新文章
                        Article article = createArticleFromEntry(entry, feed);
                        articleRepository.save(article);
                        newArticleCount++;

                        logger.debug("保存新文章: {}", article.getTitle());

                    } catch (Exception e) {
                        logger.warn("解析文章失败: {} - {}", entry.getTitle(), e.getMessage());
                    }
                }

                // 更新RSS源抓取状态
                feed.setLastFetchedAt(LocalDateTime.now());
                feed.setLastFetchedStatus("success");
                feed.setLastFetchedError(null);
                feed.setArticleCount((int) articleRepository.countByFeedId(feed.getId()));
                rssFeedRepository.save(feed);

                logger.info("RSS源 {} 抓取完成，新增 {} 篇文章", feed.getTitle(), newArticleCount);
                return newArticleCount;

            } catch (Exception e) {
                retryCount++;
                logger.error("抓取RSS源失败 (尝试 {}/{}): {} - {}",
                           retryCount, maxRetry, feed.getTitle(), e.getMessage());

                if (retryCount >= maxRetry) {
                    // 更新失败状态
                    feed.setLastFetchedAt(LocalDateTime.now());
                    feed.setLastFetchedStatus("failed");
                    feed.setLastFetchedError(e.getMessage());
                    rssFeedRepository.save(feed);

                    logger.error("RSS源 {} 抓取失败，已达到最大重试次数", feed.getTitle());
                    return 0;
                }

                // 等待后重试
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return 0;
                }
            }
        }

        return 0;
    }

    /**
     * 批量抓取所有激活的RSS源
     * @return 总共抓取到的文章数量
     */
    @Transactional
    public int fetchAllActiveFeeds() {
        logger.info("开始批量抓取所有激活的RSS源");

        List<RssFeed> feeds = rssFeedRepository.findActiveFeeds();
        int totalArticles = 0;

        for (RssFeed feed : feeds) {
            try {
                int count = fetchRssFeed(feed);
                totalArticles += count;
            } catch (Exception e) {
                logger.error("抓取RSS源异常: {} - {}", feed.getTitle(), e.getMessage());
            }
        }

        logger.info("批量抓取完成，总共新增 {} 篇文章", totalArticles);
        return totalArticles;
    }

    /**
     * 抓取需要更新的RSS源（基于抓取间隔）
     * @return 总共抓取到的文章数量
     */
    @Transactional
    public int fetchFeedsByInterval() {
        logger.info("开始抓取需要更新的RSS源");

        // 计算阈值时间（当前时间减去默认间隔30分钟）
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        List<RssFeed> feeds = rssFeedRepository.findFeedsToFetch(threshold);

        logger.info("找到 {} 个需要抓取的RSS源", feeds.size());

        int totalArticles = 0;
        for (RssFeed feed : feeds) {
            try {
                int count = fetchRssFeed(feed);
                totalArticles += count;
            } catch (Exception e) {
                logger.error("抓取RSS源异常: {} - {}", feed.getTitle(), e.getMessage());
            }
        }

        logger.info("抓取完成，总共新增 {} 篇文章", totalArticles);
        return totalArticles;
    }

    /**
     * 从SyndEntry提取GUID
     */
    private String getEntryGuid(SyndEntry entry) {
        // 优先使用URI
        if (StringUtils.isNotBlank(entry.getUri())) {
            return entry.getUri();
        }
        // 其次使用link
        if (StringUtils.isNotBlank(entry.getLink())) {
            return entry.getLink();
        }
        // 最后使用标题（作为最后手段）
        return StringUtils.defaultString(entry.getTitle(), "");
    }

    /**
     * 从SyndEntry创建Article实体
     */
    private Article createArticleFromEntry(SyndEntry entry, RssFeed feed) {
        Article article = new Article();
        article.setFeedId(feed.getId());
        article.setGuid(getEntryGuid(entry));

        // 标题
        article.setTitle(StringUtils.defaultString(entry.getTitle(), "无标题"));

        // 链接
        article.setLink(StringUtils.defaultString(entry.getLink(), ""));

        // 作者
        article.setAuthor(entry.getAuthor());

        // 描述
        if (entry.getDescription() != null) {
            article.setDescription(entry.getDescription().getValue());
        }

        // 内容（优先使用content元素）
        StringBuilder contentBuilder = new StringBuilder();
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            for (SyndContent content : entry.getContents()) {
                if (StringUtils.isNotBlank(content.getValue())) {
                    contentBuilder.append(content.getValue());
                }
            }
        }
        // 如果没有content，使用description
        if (contentBuilder.length() == 0 && entry.getDescription() != null) {
            contentBuilder.append(entry.getDescription().getValue());
        }
        article.setContent(contentBuilder.toString());

        // 发布时间
        if (entry.getPublishedDate() != null) {
            article.setPubDate(convertToLocalDateTime(entry.getPublishedDate()));
        } else if (entry.getUpdatedDate() != null) {
            article.setPubDate(convertToLocalDateTime(entry.getUpdatedDate()));
        } else {
            article.setPubDate(LocalDateTime.now());
        }

        // 分类
        article.setCategory(feed.getCategory());

        // 图片URL（尝试从enclosures中提取）
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            for (var enclosure : entry.getEnclosures()) {
                if (enclosure.getType() != null && enclosure.getType().startsWith("image/")) {
                    article.setImageUrl(enclosure.getUrl());
                    break;
                }
            }
        }

        // 浏览次数
        article.setViewCount(0);

        return article;
    }

    /**
     * 转换Date到LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
            ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
    }

    /**
     * 测试RSS源URL是否有效
     * @param url RSS源URL
     * @return 是否有效
     */
    public boolean testRssUrl(String url) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            try (XmlReader reader = new XmlReader(new URL(url))) {
                SyndFeed feed = input.build(reader);
                return feed != null && feed.getEntries() != null;
            }
        } catch (Exception e) {
            logger.warn("测试RSS URL失败: {} - {}", url, e.getMessage());
            return false;
        }
    }

    /**
     * 从RSS URL获取标题
     * @param url RSS源URL
     * @return RSS源标题，如果获取失败返回null
     */
    public String fetchTitleFromUrl(String url) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            try (XmlReader reader = new XmlReader(new URL(url))) {
                SyndFeed feed = input.build(reader);
                if (feed != null && StringUtils.isNotBlank(feed.getTitle())) {
                    return feed.getTitle();
                }
            }
        } catch (Exception e) {
            logger.debug("从RSS获取标题失败: {} - {}", url, e.getMessage());
        }
        return null;
    }
}
