package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.entity.User;
import com.newsapp.service.FileStorageService;
import com.newsapp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 头像上传控制器
 */
@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

    private static final Logger log = LoggerFactory.getLogger(AvatarController.class);

    private final FileStorageService fileStorageService;
    private final UserService userService;

    public AvatarController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    /**
     * 上传头像
     */
    @PostMapping
    public ApiResponse<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // 存储文件
            String fileUrl = fileStorageService.storeAvatar(file, currentUser.getId());

            // 删除旧头像
            String oldAvatarUrl = currentUser.getAvatarUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                fileStorageService.deleteFile(oldAvatarUrl);
            }

            // 更新用户头像URL
            userService.updateAvatarUrl(currentUser.getId(), fileUrl);
            currentUser.setAvatarUrl(fileUrl);
            session.setAttribute("user", currentUser);

            log.info("头像上传成功: userId={}, fileUrl={}", currentUser.getId(), fileUrl);

            Map<String, String> result = new HashMap<>();
            result.put("avatarUrl", fileUrl);
            return ApiResponse.success("头像上传成功", result);
        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
