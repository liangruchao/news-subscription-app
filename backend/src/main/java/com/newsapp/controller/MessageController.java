package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.SendMessageRequest;
import com.newsapp.entity.Message;
import com.newsapp.entity.User;
import com.newsapp.service.MessageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 */
@RestController
@RequestMapping("/api/messages")
@Validated
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 获取所有消息
     */
    @GetMapping
    public ApiResponse<List<Message>> getMessages(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<Message> messages = messageService.getUserMessages(currentUser.getId());
            return ApiResponse.success(messages);
        } catch (Exception e) {
            log.error("获取消息列表失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取未读消息
     */
    @GetMapping("/unread")
    public ApiResponse<List<Message>> getUnreadMessages(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            List<Message> messages = messageService.getUnreadMessages(currentUser.getId());
            return ApiResponse.success(messages);
        } catch (Exception e) {
            log.error("获取未读消息失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Long>> getUnreadCount(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            long count = messageService.getUnreadCount(currentUser.getId());
            Map<String, Long> result = new HashMap<>();
            result.put("count", count);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取未读消息数量失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            messageService.markAsRead(id, currentUser.getId());
            return ApiResponse.success("标记成功", null);
        } catch (Exception e) {
            log.error("标记消息已读失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            messageService.markAllAsRead(currentUser.getId());
            return ApiResponse.success("全部标记为已读", null);
        } catch (Exception e) {
            log.error("标记所有消息已读失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMessage(@PathVariable Long id, HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            messageService.deleteMessage(id, currentUser.getId());
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            log.error("删除消息失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送消息（管理员功能）
     */
    @PostMapping("/send")
    public ApiResponse<Message> sendMessage(
            @RequestParam Long userId,
            @Valid @RequestBody SendMessageRequest request,
            HttpSession session) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return ApiResponse.error("未登录");
            }

            // 设置发送者ID
            request.setSenderId(currentUser.getId());

            Message message = messageService.sendMessage(userId, request);
            return ApiResponse.success("发送成功", message);
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }
}
