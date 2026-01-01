package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.LoginRequest;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.entity.User;
import com.newsapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<User> register(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            User user = userService.register(request);
            // 自动登录，保存到 session
            session.setAttribute("user", user);
            return ApiResponse.success("注册成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<User> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            User user = userService.login(request);
            // 保存到 session
            session.setAttribute("user", user);
            return ApiResponse.success("登录成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ApiResponse.error("未登录");
        }
        return ApiResponse.success(user);
    }
}
