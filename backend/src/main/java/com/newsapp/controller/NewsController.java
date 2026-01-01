package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.NewsDto;
import com.newsapp.entity.Subscription;
import com.newsapp.entity.User;
import com.newsapp.service.NewsService;
import com.newsapp.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新闻控制器
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;
    private final SubscriptionService subscriptionService;

    public NewsController(NewsService newsService, SubscriptionService subscriptionService) {
        this.newsService = newsService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * 获取当前用户订阅的新闻
     */
    @GetMapping
    public ApiResponse<List<NewsDto>> getUserNews(HttpSession session) {
        try {
            User user = getCurrentUser(session);
            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user.getId());

            if (subscriptions.isEmpty()) {
                return ApiResponse.error("请先订阅新闻类别");
            }

            List<String> categories = subscriptions.stream()
                    .map(Subscription::getCategory)
                    .toList();

            List<NewsDto> news = newsService.getUserNews(categories);
            return ApiResponse.success(news);

        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取指定类别的新闻
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<NewsDto>> getNewsByCategory(@PathVariable String category) {
        try {
            List<NewsDto> news = newsService.getNewsByCategory(category);
            return ApiResponse.success(news);
        } catch (RuntimeException e) {
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
