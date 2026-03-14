package com.newsapp.service;

import com.newsapp.dto.ArticleShareDto;
import com.newsapp.dto.ShareArticleRequest;
import com.newsapp.entity.Article;
import com.newsapp.entity.ArticleShare;
import com.newsapp.repository.ArticleRepository;
import com.newsapp.repository.ArticleShareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文章分享服务
 */
@Service
public class ArticleShareService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleShareService.class);

    @Value("${share.default.expire.days:7}")
    private int defaultExpireDays;

    @Value("${share.max.per.user.day:20}")
    private int maxSharesPerUserDay;

    @Value("${app.base.url:http://localhost:8081}")
    private String baseUrl;

    private final ArticleShareRepository shareRepository;
    private final ArticleRepository articleRepository;

    public ArticleShareService(ArticleShareRepository shareRepository, ArticleRepository articleRepository) {
        this.shareRepository = shareRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * 创建文章分享
     */
    @Transactional
    public ArticleShareDto createShare(Long userId, ShareArticleRequest request) {
        logger.info("用户 {} 分享文章 {}", userId, request.getArticleId());

        // 检查文章是否存在
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        // 检查用户今日分享数量限制
        long todayCount = shareRepository.countByUserIdToday(userId);
        if (todayCount >= maxSharesPerUserDay) {
            throw new IllegalArgumentException("今日分享数量已达上限：" + maxSharesPerUserDay);
        }

        // 创建分享记录
        ArticleShare share = new ArticleShare(userId, request.getArticleId());
        share.setShareCode(generateShareCode());
        share.setTitle(request.getTitle());
        share.setDescription(request.getDescription());

        // 设置过期时间
        int expireDays = request.getExpireDays() != null ? request.getExpireDays() : defaultExpireDays;
        if (expireDays > 0) {
            share.setExpiresAt(LocalDateTime.now().plusDays(expireDays));
        }

        share = shareRepository.save(share);
        logger.info("创建分享成功: shareCode={}", share.getShareCode());

        return convertToDto(share, article);
    }

    /**
     * 根据分享码获取分享内容
     */
    @Transactional
    public ArticleShareDto getShareByCode(String shareCode) {
        ArticleShare share = shareRepository.findActiveShare(shareCode, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("分享不存在或已过期"));

        // 增加浏览次数
        shareRepository.incrementViewCount(share.getId());
        share.setViewCount(share.getViewCount() + 1);

        // 获取文章信息
        Article article = articleRepository.findById(share.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));

        return convertToDto(share, article);
    }

    /**
     * 获取用户的分享列表
     */
    public List<ArticleShareDto> getUserShares(Long userId) {
        List<ArticleShare> shares = shareRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return shares.stream()
                .map(share -> {
                    Article article = articleRepository.findById(share.getArticleId()).orElse(null);
                    return convertToDto(share, article);
                })
                .toList();
    }

    /**
     * 删除分享
     */
    @Transactional
    public void deleteShare(Long userId, Long shareId) {
        ArticleShare share = shareRepository.findById(shareId)
                .orElseThrow(() -> new IllegalArgumentException("分享不存在"));

        if (!share.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此分享");
        }

        shareRepository.deleteById(shareId);
        logger.info("删除分享: shareId={}", shareId);
    }

    /**
     * 清理过期的分享
     */
    @Transactional
    public int cleanExpiredShares() {
        List<ArticleShare> expiredShares = shareRepository.findExpiredShares(LocalDateTime.now());

        for (ArticleShare share : expiredShares) {
            share.setIsActive(false);
            shareRepository.save(share);
        }

        logger.info("标记过期分享为失效: {} 条", expiredShares.size());

        // 删除30天前的失效分享
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deleted = shareRepository.deleteOldExpiredShares(thirtyDaysAgo);
        logger.info("删除过期分享: {} 条", deleted);

        return expiredShares.size();
    }

    /**
     * 生成分享码
     */
    private String generateShareCode() {
        // 生成32位随机字符串
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 转换为DTO
     */
    private ArticleShareDto convertToDto(ArticleShare share, Article article) {
        ArticleShareDto dto = new ArticleShareDto();
        dto.setId(share.getId());
        dto.setShareCode(share.getShareCode());
        dto.setShareUrl(baseUrl + "/share/" + share.getShareCode());
        dto.setArticleId(share.getArticleId());

        if (article != null) {
            dto.setArticleTitle(article.getTitle());
            dto.setArticleContent(article.getContent());
            dto.setArticleLink(article.getLink());
            dto.setArticleImageUrl(article.getImageUrl());
            dto.setArticleAuthor(article.getAuthor());
            dto.setArticlePubDate(article.getPubDate());
        }

        dto.setTitle(share.getTitle());
        dto.setDescription(share.getDescription());
        dto.setViewCount(share.getViewCount());
        dto.setExpiresAt(share.getExpiresAt());
        dto.setCreatedAt(share.getCreatedAt());

        return dto;
    }
}
