package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.UserStatisticsResponse;
import com.newsapp.entity.User;
import com.newsapp.service.StatisticsService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping
    public ApiResponse<UserStatisticsResponse> getStatistics(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            UserStatisticsResponse stats = statisticsService.getUserStatistics(currentUser);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
