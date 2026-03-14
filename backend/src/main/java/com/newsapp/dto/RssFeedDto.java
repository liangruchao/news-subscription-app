package com.newsapp.dto;

import java.time.LocalDateTime;

/**
 * RSS源数据传输对象
 */
public class RssFeedDto {
    private Long id;
    private String title;
    private String url;
    private String description;
    private String category;
    private String language;
    private String iconUrl;
    private Boolean isActive;
    private LocalDateTime lastFetchedAt;
    private String lastFetchedStatus;
    private String lastFetchedError;
    private Integer fetchInterval;
    private Integer articleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getLastFetchedAt() {
        return lastFetchedAt;
    }

    public void setLastFetchedAt(LocalDateTime lastFetchedAt) {
        this.lastFetchedAt = lastFetchedAt;
    }

    public String getLastFetchedStatus() {
        return lastFetchedStatus;
    }

    public void setLastFetchedStatus(String lastFetchedStatus) {
        this.lastFetchedStatus = lastFetchedStatus;
    }

    public String getLastFetchedError() {
        return lastFetchedError;
    }

    public void setLastFetchedError(String lastFetchedError) {
        this.lastFetchedError = lastFetchedError;
    }

    public Integer getFetchInterval() {
        return fetchInterval;
    }

    public void setFetchInterval(Integer fetchInterval) {
        this.fetchInterval = fetchInterval;
    }

    public Integer getArticleCount() {
        return articleCount;
    }

    public void setArticleCount(Integer articleCount) {
        this.articleCount = articleCount;
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
