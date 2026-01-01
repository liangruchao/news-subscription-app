package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.CreateAnnouncementRequest;
import com.newsapp.entity.Announcement;
import com.newsapp.entity.User;
import com.newsapp.service.AnnouncementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/announcements")
@Validated
public class AnnouncementController {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementController.class);

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 获取已发布的公告列表
     */
    @GetMapping("/published")
    public ApiResponse<List<Announcement>> getPublishedAnnouncements() {
        try {
            List<Announcement> announcements = announcementService.getPublishedAnnouncements();
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            log.error("获取已发布公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有公告（管理员）
     */
    @GetMapping
    public ApiResponse<List<Announcement>> getAllAnnouncements(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // TODO: 添加管理员权限检查
            List<Announcement> announcements = announcementService.getAllAnnouncements();
            return ApiResponse.success(announcements);
        } catch (Exception e) {
            log.error("获取所有公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取公告
     */
    @GetMapping("/{id}")
    public ApiResponse<Announcement> getAnnouncementById(@PathVariable Long id) {
        try {
            Announcement announcement = announcementService.getAnnouncementById(id);
            return ApiResponse.success(announcement);
        } catch (Exception e) {
            log.error("获取公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建公告（管理员）
     */
    @PostMapping
    public ApiResponse<Announcement> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // TODO: 添加管理员权限检查
            Announcement announcement = announcementService.createAnnouncement(currentUser.getId(), request);
            log.info("公告创建成功: announcementId={}", announcement.getId());
            return ApiResponse.success("创建成功", announcement);
        } catch (Exception e) {
            log.error("创建公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发布公告（管理员）
     */
    @PutMapping("/{id}/publish")
    public ApiResponse<Announcement> publishAnnouncement(
            @PathVariable Long id,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // TODO: 添加管理员权限检查
            Announcement announcement = announcementService.publishAnnouncement(id);
            log.info("公告发布成功: announcementId={}", id);
            return ApiResponse.success("发布成功", announcement);
        } catch (Exception e) {
            log.error("发布公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/{id}")
    public ApiResponse<Announcement> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody CreateAnnouncementRequest request,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // TODO: 添加管理员权限检查
            Announcement announcement = announcementService.updateAnnouncement(id, request);
            log.info("公告更新成功: announcementId={}", id);
            return ApiResponse.success("更新成功", announcement);
        } catch (Exception e) {
            log.error("更新公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAnnouncement(
            @PathVariable Long id,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // TODO: 添加管理员权限检查
            announcementService.deleteAnnouncement(id);
            log.info("公告删除成功: announcementId={}", id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除公告失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
