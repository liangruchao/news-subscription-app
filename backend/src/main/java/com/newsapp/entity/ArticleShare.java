package com.newsapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文章分享实体
 */
@Entity
@Table(name = "article_shares")
public class ArticleShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分享码（32位随机字符串）
     */
    @Column(name = "share_code", length = 32, nullable = false, unique = true)
    private String shareCode;

    /**
     * 分享用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 文章ID
     */
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /**
     * 自定义标题
     */
    @Column(name = "title", length = 500)
    private String title;

    /**
     * 自定义描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 浏览次数
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 是否激活
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public ArticleShare() {
    }

    public ArticleShare(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
