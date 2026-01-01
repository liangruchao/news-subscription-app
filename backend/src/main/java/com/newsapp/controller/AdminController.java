package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 临时管理工具 - 用于修复数据库问题
 * 生产环境应该删除此控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 修复所有明文密码
     * 警告：此方法仅用于开发/调试，生产环境应删除
     */
    @PostMapping("/fix-passwords")
    public ApiResponse<Map<String, Object>> fixPasswords() {
        log.info("开始修复明文密码...");

        Map<String, Object> result = new HashMap<>();
        int fixedCount = 0;
        int skippedCount = 0;

        var users = userRepository.findAll();

        for (var user : users) {
            String password = user.getPassword();

            // 检查是否已经是 BCrypt 格式
            if (password != null && password.startsWith("$2a$")) {
                skippedCount++;
                log.debug("跳过已加密用户: {}", user.getUsername());
            } else if (password != null && !password.isEmpty()) {
                // 加密明文密码
                String newHash = passwordEncoder.encode(password);
                user.setPassword(newHash);
                userRepository.save(user);
                fixedCount++;
                log.info("修复用户: {} | 原密码: {} | 新哈希: {}", user.getUsername(), password, newHash);
            } else {
                log.warn("跳过空密码用户: {}", user.getUsername());
            }
        }

        result.put("fixedCount", fixedCount);
        result.put("skippedCount", skippedCount);
        result.put("totalCount", users.size());

        log.info("密码修复完成！修复: {}, 跳过: {}, 总计: {}", fixedCount, skippedCount, users.size());

        return ApiResponse.success("密码修复完成", result);
    }
}
