package com.newsapp.entity;

import jakarta.persistence.*;

/**
 * 用户偏好设置实体
 */
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    // 通知偏好
    @Column(name = "email_notification", nullable = false)
    private Boolean emailNotification = true;

    @Column(name = "daily_digest", nullable = false)
    private Boolean dailyDigest = false;

    @Column(name = "subscription_update", nullable = false)
    private Boolean subscriptionUpdate = true;

    @Column(name = "news_notification", nullable = false)
    private Boolean newsNotification = true;

    @Column(name = "system_notification", nullable = false)
    private Boolean systemNotification = true;

    @Column(name = "subscription_notification", nullable = false)
    private Boolean subscriptionNotification = true;

    // 界面偏好
    @Column(name = "theme", length = 20)
    private String theme = "light";

    @Column(name = "language", length = 10)
    private String language = "zh-CN";

    @Column(name = "page_size")
    private Integer pageSize = 10;

    @Column(name = "news_page_size")
    private Integer newsPageSize = 10;

    @Column(name = "compact_mode", nullable = false)
    private Boolean compactMode = false;

    // 隐私偏好
    @Column(name = "public_profile", nullable = false)
    private Boolean publicProfile = false;

    @Column(name = "show_online_status", nullable = false)
    private Boolean showOnlineStatus = true;

    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDateTime.now();
        updatedAt = java.time.LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }

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

    public Boolean getEmailNotification() {
        return emailNotification;
    }

    public void setEmailNotification(Boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public Boolean getDailyDigest() {
        return dailyDigest;
    }

    public void setDailyDigest(Boolean dailyDigest) {
        this.dailyDigest = dailyDigest;
    }

    public Boolean getSubscriptionUpdate() {
        return subscriptionUpdate;
    }

    public void setSubscriptionUpdate(Boolean subscriptionUpdate) {
        this.subscriptionUpdate = subscriptionUpdate;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getNewsNotification() {
        return newsNotification;
    }

    public void setNewsNotification(Boolean newsNotification) {
        this.newsNotification = newsNotification;
    }

    public Boolean getSystemNotification() {
        return systemNotification;
    }

    public void setSystemNotification(Boolean systemNotification) {
        this.systemNotification = systemNotification;
    }

    public Boolean getSubscriptionNotification() {
        return subscriptionNotification;
    }

    public void setSubscriptionNotification(Boolean subscriptionNotification) {
        this.subscriptionNotification = subscriptionNotification;
    }

    public Integer getNewsPageSize() {
        return newsPageSize;
    }

    public void setNewsPageSize(Integer newsPageSize) {
        this.newsPageSize = newsPageSize;
    }

    public Boolean getCompactMode() {
        return compactMode;
    }

    public void setCompactMode(Boolean compactMode) {
        this.compactMode = compactMode;
    }

    public Boolean getPublicProfile() {
        return publicProfile;
    }

    public void setPublicProfile(Boolean publicProfile) {
        this.publicProfile = publicProfile;
    }

    public Boolean getShowOnlineStatus() {
        return showOnlineStatus;
    }

    public void setShowOnlineStatus(Boolean showOnlineStatus) {
        this.showOnlineStatus = showOnlineStatus;
    }
}
