package com.newsapp.dto;

/**
 * 更新偏好设置请求
 */
public class UpdatePreferenceRequest {

    // 通知偏好
    private Boolean emailNotification;
    private Boolean dailyDigest;
    private Boolean subscriptionUpdate;

    // 界面偏好
    private String theme;
    private String language;
    private Integer pageSize;

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
}
