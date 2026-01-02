package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.LoginHistoryDTO;
import com.newsapp.dto.UserProfileDTO;
import com.newsapp.dto.UserStatsDTO;
import com.newsapp.entity.User;
import com.newsapp.service.LoginHistoryService;
import com.newsapp.service.MessageService;
import com.newsapp.service.StatisticsService;
import com.newsapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 用户个人中心 Controller
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginHistoryService loginHistoryService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private StatisticsService statisticsService;

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
     * 获取用户资料
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> getProfile(HttpSession session) {
        User user = getCurrentUser(session);
        // 重新从数据库获取最新数据
        user = userService.findByUsername(user.getUsername());

        UserProfileDTO profile = new UserProfileDTO();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setEmail(user.getEmail());
        profile.setAvatarUrl(user.getAvatarUrl());
        profile.setBio(user.getBio());
        profile.setCreatedAt(user.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * 更新用户资料
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateProfile(
            @RequestBody Map<String, String> updates,
            HttpSession session) {
        User user = getCurrentUser(session);
        // 重新从数据库获取最新数据
        user = userService.findByUsername(user.getUsername());

        if (updates.containsKey("bio")) {
            user.setBio(updates.get("bio"));
        }

        User updatedUser = userService.updateUser(user);
        // 更新 session 中的用户信息
        session.setAttribute("user", updatedUser);

        UserProfileDTO profile = new UserProfileDTO();
        profile.setId(updatedUser.getId());
        profile.setUsername(updatedUser.getUsername());
        profile.setEmail(updatedUser.getEmail());
        profile.setAvatarUrl(updatedUser.getAvatarUrl());
        profile.setBio(updatedUser.getBio());
        profile.setCreatedAt(updatedUser.getCreatedAt());

        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> passwordData,
            HttpSession session) {
        User user = getCurrentUser(session);
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        userService.changePassword(user.getUsername(), oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 上传头像
     */
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        User user = getCurrentUser(session);
        String avatarUrl = userService.uploadAvatar(user.getUsername(), file);

        // 更新 session 中的用户信息
        user = userService.findByUsername(user.getUsername());
        session.setAttribute("user", user);

        return ResponseEntity.ok(ApiResponse.success(Map.of("avatarUrl", avatarUrl)));
    }

    /**
     * 获取登录历史
     */
    @GetMapping("/login-history")
    public ResponseEntity<ApiResponse<List<LoginHistoryDTO>>> getLoginHistory(HttpSession session) {
        User user = getCurrentUser(session);
        List<LoginHistoryDTO> history = loginHistoryService.getUserLoginHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getStats(HttpSession session) {
        User user = getCurrentUser(session);
        UserStatsDTO stats = statisticsService.getUserStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 删除账户
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAccount(HttpSession session) {
        User user = getCurrentUser(session);
        userService.deleteUser(user.getUsername());
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
