package com.newsapp.dto;

import java.time.LocalDateTime;

/**
 * 用户收藏DTO
 */
public class UserFavoriteDto {
    private Long id;
    private Long userId;
    private Long articleId;
    private String articleTitle;
    private String articleDescription;
    private String articleLink;
    private String articleImageUrl;
    private LocalDateTime articlePubDate;
    private String notes;
    private String tags;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleDescription() {
        return articleDescription;
    }

    public void setArticleDescription(String articleDescription) {
        this.articleDescription = articleDescription;
    }

    public String getArticleLink() {
        return articleLink;
    }

    public void setArticleLink(String articleLink) {
        this.articleLink = articleLink;
    }

    public String getArticleImageUrl() {
        return articleImageUrl;
    }

    public void setArticleImageUrl(String articleImageUrl) {
        this.articleImageUrl = articleImageUrl;
    }

    public LocalDateTime getArticlePubDate() {
        return articlePubDate;
    }

    public void setArticlePubDate(LocalDateTime articlePubDate) {
        this.articlePubDate = articlePubDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
