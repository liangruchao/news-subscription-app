package com.newsapp.dto;

import com.newsapp.entity.User;

/**
 * 登录响应 DTO
 * 包含用户信息和 JWT token
 */
public class LoginResponse {

    private User user;
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;

    public LoginResponse() {
    }

    public LoginResponse(User user, String token, Long expiresIn) {
        this.user = user;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
