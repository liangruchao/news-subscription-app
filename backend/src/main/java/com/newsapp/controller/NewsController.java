package com.newsapp.controller;

import com.newsapp.constants.ErrorMessages;
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
public class NewsController extends BaseController {

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
            logApiCall("GET /api/news", user.getId());

            List<Subscription> subscriptions = subscriptionService.getUserSubscriptions(user.getId());

            if (subscriptions.isEmpty()) {
                logger.warn("用户 {} 没有订阅任何类别", user.getId());
                return ApiResponse.error(ErrorMessages.NO_SUBSCRIPTIONS);
            }

            List<String> categories = subscriptions.stream()
                    .map(Subscription::getCategory)
                    .toList();

            logger.info("用户 {} 订阅的类别: {}", user.getId(), categories);

            List<NewsDto> news = newsService.getUserNews(categories);
            logger.info("为用户 {} 获取到 {} 条新闻", user.getId(), news.size());

            return ApiResponse.success(news);

        } catch (Exception e) {
            logError("GET /api/news", null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取指定类别的新闻
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<NewsDto>> getNewsByCategory(@PathVariable String category) {
        try {
            logger.info("获取指定类别的新闻: category={}", category);
            List<NewsDto> news = newsService.getNewsByCategory(category);
            logger.info("获取到 {} 条 {} 类别的新闻", news.size(), category);
            return ApiResponse.success(news);
        } catch (Exception e) {
            logError("GET /api/news/category/" + category, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
