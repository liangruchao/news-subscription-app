package com.newsapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.dto.NewsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    public NewsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取指定类别的新闻
     */
    public List<NewsDto> getNewsByCategory(String category) {
        try {
            String url = baseUrl + "/top-headlines?category=" + category +
                         "&apiKey=" + apiKey +
                         "&pageSize=" + pageSize +
                         "&language=en";

            logger.info("请求 NewsAPI: {}", url.replace(apiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);
            return parseNewsResponse(response);

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
}
