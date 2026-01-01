package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.LoginHistoryResponse;
import com.newsapp.entity.User;
import com.newsapp.service.LoginHistoryService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录历史控制器
 */
@RestController
@RequestMapping("/api/login-history")
public class LoginHistoryController {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryController.class);

    private final LoginHistoryService loginHistoryService;

    public LoginHistoryController(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    /**
     * 获取最近登录记录
     */
    @GetMapping
    public ApiResponse<List<LoginHistoryResponse>> getRecentLogins(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<LoginHistoryResponse> histories = loginHistoryService.getRecentLogins(currentUser.getId(), limit);
            return ApiResponse.success(histories);
        } catch (Exception e) {
            log.error("获取登录历史失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
