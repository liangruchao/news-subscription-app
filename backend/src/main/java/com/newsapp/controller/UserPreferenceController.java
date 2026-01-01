package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.UpdatePreferenceRequest;
import com.newsapp.entity.User;
import com.newsapp.entity.UserPreference;
import com.newsapp.service.UserPreferenceService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户偏好设置控制器
 */
@RestController
@RequestMapping("/api/preferences")
@Validated
public class UserPreferenceController {

    private static final Logger log = LoggerFactory.getLogger(UserPreferenceController.class);

    private final UserPreferenceService preferenceService;

    public UserPreferenceController(UserPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    /**
     * 获取用户偏好设置
     */
    @GetMapping
    public ApiResponse<UserPreference> getPreferences(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            UserPreference preference = preferenceService.getUserPreference(currentUser.getId());
            return ApiResponse.success(preference);
        } catch (Exception e) {
            log.error("获取用户偏好设置失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户偏好设置
     */
    @PutMapping
    public ApiResponse<UserPreference> updatePreferences(
            @RequestBody UpdatePreferenceRequest request,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            UserPreference updated = preferenceService.updatePreference(currentUser.getId(), request);
            log.info("用户偏好设置更新成功: userId={}", currentUser.getId());
            return ApiResponse.success("偏好设置更新成功", updated);
        } catch (Exception e) {
            log.error("更新用户偏好设置失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
