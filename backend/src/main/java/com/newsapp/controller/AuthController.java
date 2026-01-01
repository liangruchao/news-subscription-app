package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.LoginRequest;
import com.newsapp.dto.LoginResponse;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.entity.User;
import com.newsapp.service.UserService;
import com.newsapp.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 支持 JWT 认证
 */
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户注册
     * 注册成功后返回 JWT token
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            // 生成 JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            LoginResponse response = new LoginResponse(user, token, jwtUtil.getExpirationTime());
            log.info("用户注册成功: {}", user.getUsername());
            return ApiResponse.success("注册成功", response);
        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     * 登录成功后返回 JWT token
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            // 生成 JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            LoginResponse response = new LoginResponse(user, token, jwtUtil.getExpirationTime());
            log.info("用户登录成功: {}", user.getUsername());
            return ApiResponse.success("登录成功", response);
        } catch (RuntimeException e) {
            log.error("登录失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     * JWT 无状态，客户端只需删除 token
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("用户登出: {}", authentication != null ? authentication.getName() : "unknown");
        // JWT 无状态，客户端负责删除 token
        SecurityContextHolder.clearContext();
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 获取当前登录用户
     * 从 JWT token 中获取用户信息
     */
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ApiResponse.error("未登录");
        }

        // 从 authentication 中获取 userId
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.getUserById(userId);

        return ApiResponse.success(user);
    }
}
