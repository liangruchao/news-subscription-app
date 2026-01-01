package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.NewsDto;
import com.newsapp.entity.Subscription;
import com.newsapp.entity.User;
import com.newsapp.service.NewsService;
import com.newsapp.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新闻控制器
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);

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
            log.info("获取用户订阅的新闻: userId={}", user.getId());

            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user.getId());

            if (subscriptions.isEmpty()) {
                log.warn("用户 {} 没有订阅任何类别", user.getId());
                return ApiResponse.error("请先订阅新闻类别");
            }

            List<String> categories = subscriptions.stream()
                    .map(Subscription::getCategory)
                    .toList();

            log.info("用户 {} 订阅的类别: {}", user.getId(), categories);

            List<NewsDto> news = newsService.getUserNews(categories);
            log.info("为用户 {} 获取到 {} 条新闻", user.getId(), news.size());

            return ApiResponse.success(news);

        } catch (RuntimeException e) {
            log.error("获取用户新闻失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取指定类别的新闻
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<NewsDto>> getNewsByCategory(@PathVariable String category) {
        try {
            log.info("获取指定类别的新闻: category={}", category);
            List<NewsDto> news = newsService.getNewsByCategory(category);
            log.info("获取到 {} 条 {} 类别的新闻", news.size(), category);
            return ApiResponse.success(news);
        } catch (RuntimeException e) {
            log.error("获取类别 {} 的新闻失败: {}", category, e.getMessage());
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
