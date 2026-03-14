package com.newsapp.controller;

import com.newsapp.dto.ApiResponse;
import com.newsapp.dto.ArticleShareDto;
import com.newsapp.dto.ShareArticleRequest;
import com.newsapp.entity.User;
import com.newsapp.service.ArticleShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * 文章分享控制器
 */
@RestController
@RequestMapping("/shares")
public class ArticleShareController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleShareController.class);

    private final ArticleShareService shareService;

    public ArticleShareController(ArticleShareService shareService) {
        this.shareService = shareService;
    }

    /**
     * 创建文章分享（需要登录）
     */
    @PostMapping
    public ResponseEntity<ArticleShareDto> createShare(
            @RequestBody ShareArticleRequest request,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            ArticleShareDto share = shareService.createShare(user.getId(), request);
            return ResponseEntity.ok(share);
        } catch (IllegalArgumentException e) {
            logger.error("创建分享失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用户的分享列表（需要登录）
     */
    @GetMapping
    public ResponseEntity<List<ArticleShareDto>> getUserShares(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<ArticleShareDto> shares = shareService.getUserShares(user.getId());
        return ResponseEntity.ok(shares);
    }

    /**
     * 删除分享（需要登录）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShare(
            @PathVariable Long id,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            shareService.deleteShare(user.getId(), id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("删除分享失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据分享码获取分享内容（公开访问，无需登录）
     */
    @GetMapping("/public/{shareCode}")
    public ResponseEntity<ArticleShareDto> getShare(@PathVariable String shareCode) {
        try {
            ArticleShareDto share = shareService.getShareByCode(shareCode);
            return ResponseEntity.ok(share);
        } catch (IllegalArgumentException e) {
            logger.error("获取分享失败: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
