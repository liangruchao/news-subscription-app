package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.UserFavoriteDto;
import com.newsapp.entity.User;
import com.newsapp.service.UserFavoriteService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 用户收藏控制器
 */
@RestController
@RequestMapping("/api/favorites")
public class UserFavoriteController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserFavoriteController.class);

    private final UserFavoriteService favoriteService;

    public UserFavoriteController(UserFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /**
     * 获取用户的收藏列表（分页）
     */
    @GetMapping
    public ApiResponse<Page<UserFavoriteDto>> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("GET /api/favorites", user.getId());

            Page<UserFavoriteDto> favorites = favoriteService.getUserFavorites(user.getId(), page, size);

            logger.info("用户 {} 有 {} 个收藏", user.getId(), favorites.getTotalElements());
            return ApiResponse.success(favorites);

        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("GET /api/favorites", null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取用户的所有收藏
     */
    @GetMapping("/all")
    public ApiResponse<java.util.List<UserFavoriteDto>> getAllFavorites(HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("GET /api/favorites/all", user.getId());

            java.util.List<UserFavoriteDto> favorites = favoriteService.getAllUserFavorites(user.getId());

            return ApiResponse.success(favorites);

        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("GET /api/favorites/all", null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据标签筛选收藏
     */
    @GetMapping("/tag/{tag}")
    public ApiResponse<Page<UserFavoriteDto>> getFavoritesByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("GET /api/favorites/tag/" + tag, user.getId());

            Page<UserFavoriteDto> favorites = favoriteService.getUserFavoritesByTag(user.getId(), tag, page, size);

            return ApiResponse.success(favorites);

        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("GET /api/favorites/tag/" + tag, null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取未读收藏
     */
    @GetMapping("/unread")
    public ApiResponse<Page<UserFavoriteDto>> getUnreadFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("GET /api/favorites/unread", user.getId());

            Page<UserFavoriteDto> favorites = favoriteService.getUnreadFavorites(user.getId(), page, size);

            return ApiResponse.success(favorites);

        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("GET /api/favorites/unread", null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 检查是否收藏了指定文章
     */
    @GetMapping("/check/{articleId}")
    public ApiResponse<Boolean> checkFavorite(
            @PathVariable Long articleId,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);

            boolean isFavorited = favoriteService.isFavorited(user.getId(), articleId);

            return ApiResponse.success(isFavorited);

        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("GET /api/favorites/check/" + articleId, null, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 收藏文章
     */
    @PostMapping("/{articleId}")
    public ApiResponse<UserFavoriteDto> addFavorite(
            @PathVariable Long articleId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String tags,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("POST /api/favorites/" + articleId, user.getId());

            logger.info("用户 {} 收藏文章: articleId={}", user.getId(), articleId);

            UserFavoriteDto favorite = favoriteService.addFavorite(user.getId(), articleId, notes, tags);

            logger.info("收藏成功: id={}", favorite.getId());
            return ApiResponse.success("收藏成功", favorite);

        } catch (IllegalArgumentException e) {
            logger.warn("收藏失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("POST /api/favorites/" + articleId, null, e.getMessage());
            return ApiResponse.error("收藏失败: " + e.getMessage());
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{articleId}")
    public ApiResponse<Void> removeFavorite(
            @PathVariable Long articleId,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);
            logApiCall("DELETE /api/favorites/" + articleId, user.getId());

            favoriteService.removeFavorite(user.getId(), articleId);

            logger.info("取消收藏成功: userId={}, articleId={}", user.getId(), articleId);
            return ApiResponse.success("取消收藏成功", null);

        } catch (IllegalArgumentException e) {
            logger.warn("取消收藏失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("DELETE /api/favorites/" + articleId, null, e.getMessage());
            return ApiResponse.error("取消收藏失败: " + e.getMessage());
        }
    }

    /**
     * 更新收藏笔记
     */
    @PatchMapping("/{articleId}/notes")
    public ApiResponse<Void> updateNotes(
            @PathVariable Long articleId,
            @RequestParam String notes,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);

            favoriteService.updateNotes(user.getId(), articleId, notes);

            return ApiResponse.success("笔记更新成功", null);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("PATCH /api/favorites/" + articleId + "/notes", null, e.getMessage());
            return ApiResponse.error("更新笔记失败: " + e.getMessage());
        }
    }

    /**
     * 更新收藏标签
     */
    @PatchMapping("/{articleId}/tags")
    public ApiResponse<Void> updateTags(
            @PathVariable Long articleId,
            @RequestParam String tags,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);

            favoriteService.updateTags(user.getId(), articleId, tags);

            return ApiResponse.success("标签更新成功", null);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("PATCH /api/favorites/" + articleId + "/tags", null, e.getMessage());
            return ApiResponse.error("更新标签失败: " + e.getMessage());
        }
    }

    /**
     * 切换已读状态
     */
    @PatchMapping("/{articleId}/read")
    public ApiResponse<Void> toggleReadStatus(
            @PathVariable Long articleId,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);

            favoriteService.toggleReadStatus(user.getId(), articleId);

            return ApiResponse.success("状态更新成功", null);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("PATCH /api/favorites/" + articleId + "/read", null, e.getMessage());
            return ApiResponse.error("更新状态失败: " + e.getMessage());
        }
    }

    /**
     * 标记为已读
     */
    @PostMapping("/{articleId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long articleId,
            HttpSession session) {
        try {
            User user = getCurrentUser(session);

            favoriteService.markAsRead(user.getId(), articleId);

            return ApiResponse.success("已标记为已读", null);

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            return ApiResponse.error("请先登录");
        } catch (Exception e) {
            logError("POST /api/favorites/" + articleId + "/read", null, e.getMessage());
            return ApiResponse.error("标记失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有标签
     */
    @GetMapping("/tags")
    public ApiResponse<java.util.List<String>> getUserTags(HttpSession session) {
        try {
            User user = getCurrentUser(session);

            java.util.List<String> tags = favoriteService.getUserTags(user.getId());

            return ApiResponse.success(tags);

        } catch (Exception e) {
            logError("GET /api/favorites/tags", null, e.getMessage());
            return ApiResponse.error("获取标签失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏统计
     */
    @GetMapping("/stats")
    public ApiResponse<UserFavoriteStats> getUserStats(HttpSession session) {
        try {
            User user = getCurrentUser(session);

            UserFavoriteService.UserFavoriteStats stats = favoriteService.getUserStats(user.getId());

            // 创建简单的DTO返回
            UserFavoriteStats dto = new UserFavoriteStats();
            dto.totalCount = stats.getTotalCount();
            dto.unreadCount = stats.getUnreadCount();
            dto.readCount = stats.getReadCount();

            return ApiResponse.success(dto);

        } catch (Exception e) {
            logError("GET /api/favorites/stats", null, e.getMessage());
            return ApiResponse.error("获取统计失败: " + e.getMessage());
        }
    }

    /**
     * 用户收藏统计DTO
     */
    public static class UserFavoriteStats {
        public long totalCount;
        public long unreadCount;
        public long readCount;

        public long getTotalCount() {
            return totalCount;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public long getReadCount() {
            return readCount;
        }
    }
}
