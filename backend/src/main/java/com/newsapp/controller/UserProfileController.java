package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.ChangePasswordRequest;
import com.newsapp.dto.UpdateProfileRequest;
import com.newsapp.dto.UserProfileResponse;
import com.newsapp.entity.User;
import com.newsapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人中心控制器
 */
@RestController
@RequestMapping("/api/profile")
@Validated
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户资料
     */
    @GetMapping
    public ApiResponse<UserProfileResponse> getProfile(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            UserProfileResponse profile = userService.getUserProfile(currentUser.getId());
            return ApiResponse.success(profile);
        } catch (Exception e) {
            log.error("获取用户资料失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping
    public ApiResponse<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            User updatedUser = userService.updateProfile(currentUser.getId(), request);
            // 更新session中的用户信息
            session.setAttribute("user", updatedUser);

            log.info("用户资料更新成功: userId={}", updatedUser.getId());
            return ApiResponse.success("资料更新成功", updatedUser);
        } catch (Exception e) {
            log.error("更新用户资料失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            userService.changePassword(currentUser.getId(), request);
            log.info("密码修改成功: userId={}", currentUser.getId());
            return ApiResponse.success("密码修改成功", null);
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 注销账户
     */
    @DeleteMapping
    public ApiResponse<Void> deleteAccount(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            userService.deleteUser(currentUser.getId());
            session.invalidate();

            log.info("账户注销成功: userId={}", currentUser.getId());
            return ApiResponse.success("账户注销成功", null);
        } catch (Exception e) {
            log.error("账户注销失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
