package com.newsapp.dto;

import java.time.LocalDateTime;

/**
 * 登录历史 DTO
 */
public class LoginHistoryDTO {
    private Long id;
    private Long userId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
    private Boolean loginSuccess;

    public LoginHistoryDTO() {}

    public LoginHistoryDTO(Long id, Long userId, String ipAddress, String userAgent, LocalDateTime loginTime, Boolean loginSuccess) {
        this.id = id;
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.loginTime = loginTime;
        this.loginSuccess = loginSuccess;
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

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public Boolean getLoginSuccess() {
        return loginSuccess;
    }

    public void setLoginSuccess(Boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }
}
