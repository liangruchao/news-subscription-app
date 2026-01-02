package com.newsapp.dto;

import java.time.LocalDateTime;

/**
 * 用户统计数据响应
 */
public class UserStatisticsResponse {

    // 阅读统计
    private Long totalArticlesRead;
    private Long readingTimeMinutes;

    // 订阅统计
    private Long totalSubscriptions;
    private Long activeCategories;

    // 活跃度统计
    private Long daysSinceJoined;
    private LocalDateTime lastLoginTime;
    private Long loginCount;

    public Long getTotalArticlesRead() {
        return totalArticlesRead;
    }

    public void setTotalArticlesRead(Long totalArticlesRead) {
        this.totalArticlesRead = totalArticlesRead;
    }

    public Long getReadingTimeMinutes() {
        return readingTimeMinutes;
    }

    public void setReadingTimeMinutes(Long readingTimeMinutes) {
        this.readingTimeMinutes = readingTimeMinutes;
    }

    public Long getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(Long totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public Long getActiveCategories() {
        return activeCategories;
    }

    public void setActiveCategories(Long activeCategories) {
        this.activeCategories = activeCategories;
    }

    public Long getDaysSinceJoined() {
        return daysSinceJoined;
    }

    public void setDaysSinceJoined(Long daysSinceJoined) {
        this.daysSinceJoined = daysSinceJoined;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }
}
