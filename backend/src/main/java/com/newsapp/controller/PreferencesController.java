package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.entity.User;
import com.newsapp.entity.UserPreference;
import com.newsapp.service.UserPreferenceService;
import com.newsapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户偏好设置 Controller
 */
@RestController
@RequestMapping("/api/preferences")
public class PreferencesController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @Autowired
    private UserService userService;

    /**
     * 从 Session 获取当前用户
     */
    private User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user;
    }

    /**
     * 获取用户偏好设置
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserPreference>> getPreferences(HttpSession session) {
        User user = getCurrentUser(session);
        UserPreference preference = userPreferenceService.getOrCreatePreference(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success(preference));
    }

    /**
     * 更新用户偏好设置
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserPreference>> updatePreferences(
            @RequestBody Map<String, Object> updates,
            HttpSession session) {
        User user = getCurrentUser(session);
        UserPreference updated = userPreferenceService.updatePreference(user.getUsername(), updates);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    /**
     * 重置用户偏好设置为默认值
     */
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<UserPreference>> resetPreferences(HttpSession session) {
        User user = getCurrentUser(session);
        UserPreference reset = userPreferenceService.resetToDefault(user.getUsername());
        return ResponseEntity.ok(ApiResponse.success(reset));
    }
}
