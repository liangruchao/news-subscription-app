package com.newsapp.service;

import com.newsapp.dto.ArticleDetailDto;
import com.newsapp.dto.UserFavoriteDto;
import com.newsapp.entity.Article;
import com.newsapp.entity.UserFavorite;
import com.newsapp.repository.ArticleRepository;
import com.newsapp.repository.UserFavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户收藏服务
 */
@Service
public class UserFavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(UserFavoriteService.class);

    private final UserFavoriteRepository favoriteRepository;
    private final ArticleRepository articleRepository;

    public UserFavoriteService(UserFavoriteRepository favoriteRepository,
                                 ArticleRepository articleRepository) {
        this.favoriteRepository = favoriteRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * 获取用户的收藏列表（分页）
     */
    // @Cacheable(value = "userFavorites", key = "#userId + '-' + #page + '-' + #size")
    public Page<UserFavoriteDto> getUserFavorites(Long userId, int page, int size) {
        logger.info("获取用户收藏列表: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return favorites.map(this::convertToDto);
    }

    /**
     * 获取用户的所有收藏
     */
    public List<UserFavoriteDto> getAllUserFavorites(Long userId) {
        logger.info("获取用户所有收藏: userId={}", userId);

        List<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return favorites.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据标签筛选用户的收藏
     */
    public Page<UserFavoriteDto> getUserFavoritesByTag(Long userId, String tag, int page, int size) {
        logger.info("根据标签筛选用户收藏: userId={}, tag={}", userId, tag);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserFavorite> favorites = favoriteRepository.findByUserIdAndTag(userId, tag, pageable);

        return favorites.map(this::convertToDto);
    }

    /**
     * 获取用户未读的收藏
     */
    public Page<UserFavoriteDto> getUnreadFavorites(Long userId, int page, int size) {
        logger.info("获取用户未读收藏: userId={}", userId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<UserFavorite> favorites = favoriteRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);

        return favorites.map(this::convertToDto);
    }

    /**
     * 检查用户是否收藏了指定文章
     */
    public boolean isFavorited(Long userId, Long articleId) {
        return favoriteRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    /**
     * 收藏文章
     */
    @Transactional
    @CacheEvict(value = "userFavorites", allEntries = true)
    public UserFavoriteDto addFavorite(Long userId, Long articleId, String notes, String tags) {
        logger.info("用户收藏文章: userId={}, articleId={}", userId, articleId);

        // 检查文章是否存在
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在: id=" + articleId));

        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new IllegalArgumentException("已经收藏了该文章");
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setArticleId(articleId);
        favorite.setNotes(notes);
        favorite.setTags(tags);
        favorite.setIsRead(false);

        UserFavorite saved = favoriteRepository.save(favorite);
        logger.info("收藏成功: id={}", saved.getId());

        return convertToDto(saved);
    }

    /**
     * 取消收藏
     */
    @Transactional
    @CacheEvict(value = "userFavorites", allEntries = true)
    public void removeFavorite(Long userId, Long articleId) {
        logger.info("用户取消收藏: userId={}, articleId={}", userId, articleId);

        if (!favoriteRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new IllegalArgumentException("未收藏该文章");
        }

        favoriteRepository.deleteByUserIdAndArticleId(userId, articleId);
        logger.info("取消收藏成功");
    }

    /**
     * 更新收藏笔记
     */
    @Transactional
    @CacheEvict(value = "userFavorites", allEntries = true)
    public void updateNotes(Long userId, Long articleId, String notes) {
        logger.info("更新收藏笔记: userId={}, articleId={}", userId, articleId);

        UserFavorite favorite = favoriteRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new IllegalArgumentException("未收藏该文章"));

        favorite.setNotes(notes);
        favoriteRepository.save(favorite);

        logger.info("收藏笔记更新成功");
    }

    /**
     * 更新收藏标签
     */
    @Transactional
    @CacheEvict(value = "userFavorites", allEntries = true)
    public void updateTags(Long userId, Long articleId, String tags) {
        logger.info("更新收藏标签: userId={}, articleId={}, tags={}", userId, articleId, tags);

        UserFavorite favorite = favoriteRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new IllegalArgumentException("未收藏该文章"));

        favorite.setTags(tags);
        favoriteRepository.save(favorite);

        logger.info("收藏标签更新成功");
    }

    /**
     * 切换已读状态
     */
    @Transactional
    @CacheEvict(value = "userFavorites", allEntries = true)
    public void toggleReadStatus(Long userId, Long articleId) {
        logger.info("切换收藏已读状态: userId={}, articleId={}", userId, articleId);

        UserFavorite favorite = favoriteRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new IllegalArgumentException("未收藏该文章"));

        favorite.setIsRead(!favorite.getIsRead());
        favoriteRepository.save(favorite);

        logger.info("已读状态切换: isRead={}", favorite.getIsRead());
    }

    /**
     * 标记为已读
     */
    @Transactional
    @CacheEvict(value = "userFavorites", allEntries = true)
    public void markAsRead(Long userId, Long articleId) {
        logger.info("标记收藏为已读: userId={}, articleId={}", userId, articleId);

        UserFavorite favorite = favoriteRepository.findByUserIdAndArticleId(userId, articleId)
                .orElseThrow(() -> new IllegalArgumentException("未收藏该文章"));

        favorite.setIsRead(true);
        favoriteRepository.save(favorite);

        logger.info("已标记为已读");
    }

    /**
     * 获取用户的所有标签
     */
    public List<String> getUserTags(Long userId) {
        logger.info("获取用户标签: userId={}", userId);

        return favoriteRepository.findDistinctTagsByUserId(userId);
    }

    /**
     * 获取用户收藏统计
     */
    public UserFavoriteStats getUserStats(Long userId) {
        long total = favoriteRepository.countByUserId(userId);
        long unread = favoriteRepository.countByUserIdAndIsReadFalse(userId);

        return new UserFavoriteStats(total, unread);
    }

    /**
     * 转换为DTO
     */
    private UserFavoriteDto convertToDto(UserFavorite favorite) {
        UserFavoriteDto dto = new UserFavoriteDto();
        dto.setId(favorite.getId());
        dto.setUserId(favorite.getUserId());
        dto.setArticleId(favorite.getArticleId());
        dto.setNotes(favorite.getNotes());
        dto.setTags(favorite.getTags());
        dto.setIsRead(favorite.getIsRead());
        dto.setCreatedAt(favorite.getCreatedAt());
        dto.setUpdatedAt(favorite.getUpdatedAt());

        // 获取文章信息
        articleRepository.findById(favorite.getArticleId()).ifPresent(article -> {
            dto.setArticleTitle(article.getTitle());
            dto.setArticleDescription(article.getDescription());
            dto.setArticleLink(article.getLink());
            dto.setArticleImageUrl(article.getImageUrl());
            dto.setArticlePubDate(article.getPubDate());
        });

        return dto;
    }

    /**
     * 清除缓存
     */
    @CacheEvict(value = "userFavorites", allEntries = true)
    public void clearCache() {
        logger.info("清除收藏缓存");
    }

    /**
     * 用户收藏统计
     */
    public static class UserFavoriteStats {
        private final long totalCount;
        private final long unreadCount;

        public UserFavoriteStats(long totalCount, long unreadCount) {
            this.totalCount = totalCount;
            this.unreadCount = unreadCount;
        }

        public long getTotalCount() {
            return totalCount;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public long getReadCount() {
            return totalCount - unreadCount;
        }
    }
}
