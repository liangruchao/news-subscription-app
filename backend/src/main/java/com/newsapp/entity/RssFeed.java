package com.newsapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RSS源实体类
 */
@Entity
@Table(name = "rss_feeds")
public class RssFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 10)
    private String language = "zh-CN";

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "last_fetched_at")
    private LocalDateTime lastFetchedAt;

    @Column(name = "last_fetched_status", length = 50)
    private String lastFetchedStatus;

    @Column(name = "last_fetched_error", columnDefinition = "TEXT")
    private String lastFetchedError;

    @Column(name = "fetch_interval")
    private Integer fetchInterval = 30;

    @Column(name = "article_count")
    private Integer articleCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

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
