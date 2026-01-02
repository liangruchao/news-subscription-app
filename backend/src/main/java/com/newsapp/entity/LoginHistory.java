package com.newsapp.entity;

import jakarta.persistence.*;

/**
 * 登录历史实体
 */
@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "login_time", nullable = false)
    private java.time.LocalDateTime loginTime;

    @Column(name = "logout_time")
    private java.time.LocalDateTime logoutTime;

    @PrePersist
    protected void onCreate() {
        loginTime = java.time.LocalDateTime.now();
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

    public java.time.LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(java.time.LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public java.time.LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(java.time.LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }
}
