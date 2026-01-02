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

    // 界面偏好
    @Column(name = "theme", length = 20)
    private String theme = "light";

    @Column(name = "language", length = 10)
    private String language = "zh-CN";

    @Column(name = "page_size")
    private Integer pageSize = 10;

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
}
