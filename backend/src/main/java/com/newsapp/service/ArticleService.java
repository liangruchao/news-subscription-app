package com.newsapp.service;

import com.newsapp.dto.ArticleDto;
import com.newsapp.dto.ArticleDetailDto;
import com.newsapp.entity.Article;
import com.newsapp.entity.RssFeed;
import com.newsapp.entity.UserFavorite;
import com.newsapp.repository.ArticleRepository;
import com.newsapp.repository.RssFeedRepository;
import com.newsapp.repository.UserFavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务
 */
@Service
public class ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    private final ArticleRepository articleRepository;
    private final RssFeedRepository rssFeedRepository;
    private final UserFavoriteRepository favoriteRepository;

    public ArticleService(ArticleRepository articleRepository,
                          RssFeedRepository rssFeedRepository,
                          UserFavoriteRepository favoriteRepository) {
        this.articleRepository = articleRepository;
        this.rssFeedRepository = rssFeedRepository;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 获取用户订阅的文章流
     */
    public List<ArticleDto> getUserArticles(Long userId, int limit) {
        logger.info("获取用户文章流: userId={}, limit={}", userId, limit);

        // TODO: 获取用户订阅的RSS源ID列表
        // 暂时返回所有激活的RSS源的文章
        List<RssFeed> feeds = rssFeedRepository.findActiveFeeds();
        List<Long> feedIds = feeds.stream().map(RssFeed::getId).collect(Collectors.toList());

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "pubDate"));
        Page<Article> articles = articleRepository.findByFeedIdsOrderByPubDateDesc(feedIds, pageable);

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定RSS源的文章（分页）
     */
    public Page<ArticleDto> getArticlesByFeed(Long feedId, int page, int size) {
        logger.info("获取RSS源文章: feedId={}, page={}, size={}", feedId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "pubDate"));
        Page<Article> articles = articleRepository.findByFeedIdOrderByPubDateDesc(feedId, pageable);

        return articles.map(this::convertToDto);
    }

    /**
     * 获取指定分类的文章（分页）
     */
    public Page<ArticleDto> getArticlesByCategory(String category, int page, int size) {
        logger.info("获取分类文章: category={}, page={}, size={}", category, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "pubDate"));
        Page<Article> articles = articleRepository.findByCategoryOrderByPubDateDesc(category, pageable);

        return articles.map(this::convertToDto);
    }

    /**
     * 获取文章详情
     */
    @Transactional
    public ArticleDetailDto getArticleDetail(Long articleId, Long userId) {
        logger.info("获取文章详情: articleId={}, userId={}", articleId, userId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: id=" + articleId));

        // 增加浏览次数
        articleRepository.incrementViewCount(articleId);

        // 获取RSS源信息
        RssFeed feed = rssFeedRepository.findById(article.getFeedId()).orElse(null);

        // 转换为详情DTO
        ArticleDetailDto dto = convertToDetailDto(article, feed);

        // 查询用户收藏信息（如果提供了userId）
        if (userId != null) {
            favoriteRepository.findByUserIdAndArticleId(userId, articleId).ifPresent(favorite -> {
                dto.setIsFavorite(true);
                dto.setIsRead(favorite.getIsRead());
                dto.setFavoriteNotes(favorite.getNotes());
                dto.setFavoriteTags(favorite.getTags());
            });
        }

        return dto;
    }

    /**
     * 全文搜索文章
     */
    public Page<ArticleDto> searchArticles(String keyword, int page, int size) {
        logger.info("搜索文章: keyword={}, page={}, size={}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "pubDate"));
        Page<Article> articles = articleRepository.searchByKeyword(keyword, pageable);

        return articles.map(this::convertToDto);
    }

    /**
     * 获取热门文章
     */
    @Cacheable(value = "popularArticles", key = "#days")
    public List<ArticleDto> getPopularArticles(int days, int limit) {
        logger.info("获取热门文章: days={}, limit={}", days, limit);

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(0, limit);

        Page<Article> articles = articleRepository.findPopularArticles(since, pageable);

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取最新文章
     */
    @Cacheable(value = "latestArticles")
    public List<ArticleDto> getLatestArticles(int limit) {
        logger.info("获取最新文章: limit={}", limit);

        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "pubDate"));
        List<Article> articles = articleRepository.findLatestArticles(pageable);

        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private ArticleDto convertToDto(Article article) {
        ArticleDto dto = new ArticleDto();
        dto.setId(article.getId());
        dto.setFeedId(article.getFeedId());

        // 获取RSS源标题
        rssFeedRepository.findById(article.getFeedId()).ifPresent(feed -> {
            dto.setFeedTitle(feed.getTitle());
        });

        dto.setGuid(article.getGuid());
        dto.setTitle(article.getTitle());
        dto.setLink(article.getLink());
        dto.setAuthor(article.getAuthor());
        dto.setDescription(article.getDescription());
        dto.setContent(article.getContent());
        dto.setPubDate(article.getPubDate());
        dto.setCategory(article.getCategory());
        dto.setImageUrl(article.getImageUrl());
        dto.setViewCount(article.getViewCount());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());

        return dto;
    }

    /**
     * 转换为详情DTO
     */
    private ArticleDetailDto convertToDetailDto(Article article, RssFeed feed) {
        ArticleDetailDto dto = new ArticleDetailDto();

        // 基本信息
        dto.setId(article.getId());
        dto.setFeedId(article.getFeedId());
        dto.setGuid(article.getGuid());
        dto.setTitle(article.getTitle());
        dto.setLink(article.getLink());
        dto.setAuthor(article.getAuthor());
        dto.setDescription(article.getDescription());
        dto.setContent(article.getContent());
        dto.setPubDate(article.getPubDate());
        dto.setCategory(article.getCategory());
        dto.setImageUrl(article.getImageUrl());
        dto.setViewCount(article.getViewCount());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());

        // RSS源信息
        if (feed != null) {
            dto.setFeedTitle(feed.getTitle());
            dto.setFeedUrl(feed.getUrl());
        }

        // 用户相关信息（默认值）
        dto.setIsFavorite(false);
        dto.setFavoriteNotes(null);
        dto.setFavoriteTags(null);

        return dto;
    }
}
