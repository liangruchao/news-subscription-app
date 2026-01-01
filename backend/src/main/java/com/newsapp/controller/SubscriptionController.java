package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.SubscriptionRequest;
import com.newsapp.entity.Subscription;
import com.newsapp.entity.User;
import com.newsapp.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅控制器
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * 获取当前用户的订阅列表
     */
    @GetMapping
    public ApiResponse<List<Subscription>> getSubscriptions(HttpSession session) {
        try {
            User user = getCurrentUser(session);
            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user.getId());
            return ApiResponse.success(subscriptions);
        } catch (RuntimeException e) {
            log.error("获取订阅列表失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 订阅新闻类别
     */
    @PostMapping
    public ApiResponse<Subscription> subscribe(@Valid @RequestBody SubscriptionRequest request, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            Subscription subscription = subscriptionService.subscribe(user.getId(), request.getCategory());
            log.info("用户 {} 订阅了 {}", user.getUsername(), request.getCategory());
            return ApiResponse.success("订阅成功", subscription);
        } catch (RuntimeException e) {
            log.error("订阅失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 取消订阅
     */
    @DeleteMapping("/{category}")
    public ApiResponse<Void> unsubscribe(@PathVariable String category, HttpSession session) {
        try {
            User user = getCurrentUser(session);
            subscriptionService.unsubscribe(user.getId(), category);
            log.info("用户 {} 取消订阅了 {}", user.getUsername(), category);
            return ApiResponse.success("取消订阅成功", null);
        } catch (RuntimeException e) {
            log.error("取消订阅失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("请先登录");
        }
        return user;
    }
}
