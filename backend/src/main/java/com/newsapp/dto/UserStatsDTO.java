package com.newsapp.dto;

/**
 * 用户统计信息 DTO
 */
public class UserStatsDTO {
    private Integer totalSubscriptions;
    private Integer totalMessages;
    private Integer unreadMessages;
    private Integer loginCount;

    public UserStatsDTO() {}

    public UserStatsDTO(Integer totalSubscriptions, Integer totalMessages, Integer unreadMessages, Integer loginCount) {
        this.totalSubscriptions = totalSubscriptions;
        this.totalMessages = totalMessages;
        this.unreadMessages = unreadMessages;
        this.loginCount = loginCount;
    }

    public Integer getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(Integer totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public Integer getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(Integer totalMessages) {
        this.totalMessages = totalMessages;
    }

    public Integer getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(Integer unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }
}
