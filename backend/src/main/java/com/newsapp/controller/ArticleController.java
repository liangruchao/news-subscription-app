package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.ArticleDto;
import com.newsapp.dto.ArticleDetailDto;
import com.newsapp.entity.User;
import com.newsapp.service.ArticleService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 获取用户订阅的文章流
     */
    @GetMapping("/feed")
    public ApiResponse<List<ArticleDto>> getUserArticles(
            @RequestParam(defaultValue = "20") int limit,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("GET /api/articles/feed", user.getId());

            List<ArticleDto> articles = articleService.getUserArticles(user.getId(), limit);

            logger.info("为用户 {} 获取到 {} 篇文章", user.getId(), articles.size());
            return ApiResponse.success(articles);

        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("GET /api/articles/feed", null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取指定RSS源的文章（分页）
     */
    @GetMapping("/feed/{feedId}")
    public ApiResponse<Page<ArticleDto>> getArticlesByFeed(
            @PathVariable Long feedId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("获取RSS源 {} 的文章: page={}, size={}", feedId, page, size);

            Page<ArticleDto> articles = articleService.getArticlesByFeed(feedId, page, size);

            return ApiResponse.success(articles);

        } catch (Exception e) {
            logError("GET /api/articles/feed/" + feedId, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取指定分类的文章（分页）
     */
    @GetMapping("/category/{category}")
    public ApiResponse<Page<ArticleDto>> getArticlesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("获取分类 {} 的文章: page={}, size={}", category, page, size);

            Page<ArticleDto> articles = articleService.getArticlesByCategory(category, page, size);

            return ApiResponse.success(articles);

        } catch (Exception e) {
            logError("GET /api/articles/category/" + category, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/{articleId}")
    public ApiResponse<ArticleDetailDto> getArticleDetail(
            @PathVariable Long articleId,
            HttpSession session) {
        try {
            logger.info("获取文章详情: articleId={}", articleId);

            User user = (User) session.getAttribute("user");
            Long userId = user != null ? user.getId() : null;

            ArticleDetailDto article = articleService.getArticleDetail(articleId, userId);

            return ApiResponse.success(article);

        } catch (IllegalArgumentException e) {
            logger.warn("获取文章详情失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logError("GET /api/articles/" + articleId, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 搜索文章
     */
    @GetMapping("/search")
    public ApiResponse<Page<ArticleDto>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("搜索文章: keyword={}, page={}, size={}", keyword, page, size);

            Page<ArticleDto> articles = articleService.searchArticles(keyword, page, size);

            logger.info("搜索完成: keyword={}, count={}", keyword, articles.getTotalElements());
            return ApiResponse.success(articles);

        } catch (Exception e) {
            logError("GET /api/articles/search?keyword=" + keyword, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取热门文章
     */
    @GetMapping("/popular")
    public ApiResponse<List<ArticleDto>> getPopularArticles(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("获取热门文章: days={}, limit={}", days, limit);

            List<ArticleDto> articles = articleService.getPopularArticles(days, limit);

            return ApiResponse.success(articles);

        } catch (Exception e) {
            logError("GET /api/articles/popular", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取最新文章
     */
    @GetMapping("/latest")
    public ApiResponse<List<ArticleDto>> getLatestArticles(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("获取最新文章: limit={}", limit);

            List<ArticleDto> articles = articleService.getLatestArticles(limit);

            return ApiResponse.success(articles);

        } catch (Exception e) {
            logError("GET /api/articles/latest", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
