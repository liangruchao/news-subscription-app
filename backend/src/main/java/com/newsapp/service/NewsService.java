package com.newsapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.dto.NewsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 新闻服务 - 调用 NewsAPI
 */
@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    @Value("${newsapi.api-key}")
    private String apiKey;

    @Value("${newsapi.base-url}")
    private String baseUrl;

    @Value("${newsapi.page-size:10}")
    private int pageSize;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数 - 支持依赖注入
     */
    public NewsService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 默认构造函数 - 用于 Spring 容器创建
     * 注意：此构造函数主要用于测试，生产环境推荐使用依赖注入
     */
    public NewsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取指定类别的新闻
     * 使用缓存，缓存 key 为 "news:{category}"
     */
    @Cacheable(value = "news", key = "#category", unless = "#result == null || #result.isEmpty()")
    public List<NewsDto> getNewsByCategory(String category) {
        try {
            String url = baseUrl + "/top-headlines?category=" + category +
                         "&apiKey=" + apiKey +
                         "&pageSize=" + pageSize +
                         "&language=en";

            logger.info("请求 NewsAPI: {}", url.replace(apiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);
            List<NewsDto> result = parseNewsResponse(response);
            logger.info("从 NewsAPI 获取到 {} 条 {} 类别的新闻", result.size(), category);
            return result;

        } catch (Exception e) {
            logger.error("获取新闻失败: {}", e.getMessage());
            throw new RuntimeException("获取新闻失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户订阅的所有新闻
     */
    public List<NewsDto> getUserNews(List<String> categories) {
        List<NewsDto> allNews = new ArrayList<>();

        for (String category : categories) {
            try {
                List<NewsDto> news = getNewsByCategory(category);
                allNews.addAll(news);
            } catch (Exception e) {
                logger.warn("获取类别 {} 的新闻失败: {}", category, e.getMessage());
            }
        }

        return allNews;
    }

    /**
     * 解析 NewsAPI 响应
     */
    private List<NewsDto> parseNewsResponse(String jsonResponse) throws Exception {
        List<NewsDto> newsList = new ArrayList<>();

        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode articles = root.path("articles");

        for (JsonNode article : articles) {
            NewsDto news = new NewsDto();
            news.setTitle(article.path("title").asText(""));
            news.setDescription(article.path("description").asText(""));
            news.setUrl(article.path("url").asText(""));
            news.setUrlToImage(article.path("urlToImage").asText(""));
            news.setPublishedAt(article.path("publishedAt").asText(""));

            JsonNode source = article.path("source");
            news.setSource(source.path("name").asText(""));

            newsList.add(news);
        }

        return newsList;
    }

    /**
     * 预加载所有分类的新闻
     */
    @CacheEvict(value = "news", allEntries = true)
    public void preloadAllCategories() {
        logger.info("开始预加载所有分类的新闻...");

        String[] categories = {"business", "entertainment", "general",
                              "health", "science", "sports", "technology"};

        int successCount = 0;
        for (String category : categories) {
            try {
                getNewsByCategory(category);
                successCount++;
                logger.info("分类 {} 预加载成功", category);
            } catch (Exception e) {
                logger.error("分类 {} 预加载失败: {}", category, e.getMessage());
            }
        }

        logger.info("预加载完成: 成功 {}/{}", successCount, categories.length);
    }

    /**
     * 自动刷新所有新闻缓存
     */
    @CacheEvict(value = "news", allEntries = true)
    public void autoRefreshAllCache() {
        logger.info("开始自动刷新新闻缓存...");
        preloadAllCategories();
        logger.info("自动刷新完成");
    }
}
