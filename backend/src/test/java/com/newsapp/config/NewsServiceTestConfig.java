package com.newsapp.config;

import com.newsapp.dto.NewsDto;
import com.newsapp.service.NewsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * NewsService 测试配置
 * 为 NewsController 集成测试提供模拟的 NewsService
 */
@TestConfiguration
public class NewsServiceTestConfig {

    @Bean
    public NewsService newsService() {
        NewsService mockService = mock(NewsService.class);

        // 准备模拟新闻数据
        List<NewsDto> mockNewsList = createMockNewsList();

        // 配置 mock 行为
        when(mockService.getUserNews(anyList())).thenReturn(mockNewsList);
        when(mockService.getNewsByCategory(anyString())).thenReturn(mockNewsList);

        return mockService;
    }

    private List<NewsDto> createMockNewsList() {
        List<NewsDto> newsList = new ArrayList<>();

        NewsDto news1 = new NewsDto();
        news1.setTitle("Tech News 1");
        news1.setDescription("Description 1");
        news1.setUrl("https://example.com/news1");
        news1.setUrlToImage("https://example.com/image1.jpg");
        news1.setSource("Tech Source");
        news1.setPublishedAt("2024-01-01T12:00:00Z");
        newsList.add(news1);

        NewsDto news2 = new NewsDto();
        news2.setTitle("Tech News 2");
        news2.setDescription("Description 2");
        news2.setUrl("https://example.com/news2");
        news2.setUrlToImage("https://example.com/image2.jpg");
        news2.setSource("Tech Source");
        news2.setPublishedAt("2024-01-01T13:00:00Z");
        newsList.add(news2);

        return newsList;
    }
}
