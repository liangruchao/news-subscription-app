package com.newsapp.controller;

import com.newsapp.entity.User;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 控制器基类
 */
public abstract class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取当前登录用户
     */
    protected User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("用户未登录");
        }
        return user;
    }

    /**
     * 记录API调用
     */
    protected void logApiCall(String api, Long userId) {
        logger.info("API调用: {} by 用户 {}", api, userId);
    }

    /**
     * 记录错误（带用户ID）
     */
    protected void logError(String api, Long userId, String errorMessage) {
        logger.error("API错误: {} by 用户 {} - {}", api, userId, errorMessage);
    }

    /**
     * 记录错误（不带用户ID）
     */
    protected void logError(String api, String errorMessage) {
        logger.error("API错误: {} - {}", api, errorMessage);
    }
}
