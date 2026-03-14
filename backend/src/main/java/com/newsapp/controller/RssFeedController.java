package com.newsapp.controller;

import com.newsapp.dto.AddRssFeedRequest;
import com.newsapp.dto.ApiResponse;
import com.newsapp.entity.RssFeed;
import com.newsapp.entity.User;
import com.newsapp.entity.UserRssSubscription;
import com.newsapp.service.RssFeedService;
import com.newsapp.service.UserRssSubscriptionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RSS源控制器
 */
@RestController
@RequestMapping("/api/rss/feeds")
@Validated
public class RssFeedController {

    private static final Logger logger = LoggerFactory.getLogger(RssFeedController.class);

    private final RssFeedService rssFeedService;
    private final UserRssSubscriptionService subscriptionService;

    public RssFeedController(RssFeedService rssFeedService, UserRssSubscriptionService subscriptionService) {
        this.rssFeedService = rssFeedService;
        this.subscriptionService = subscriptionService;
    }

    /**
     * 获取所有RSS源（分页）
     */
    @GetMapping
    public ApiResponse<Page<RssFeed>> getAllFeeds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<RssFeed> feeds = rssFeedService.getAllFeeds(pageable);
            logger.info("获取RSS源列表: page={}, size={}, total={}", page, size, feeds.getTotalElements());

            return ApiResponse.success(feeds);
        } catch (Exception e) {
            logger.error("获取RSS源列表失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有激活的RSS源
     */
    @GetMapping("/active")
    public ApiResponse<List<RssFeed>> getActiveFeeds() {
        try {
            List<RssFeed> feeds = rssFeedService.getActiveFeeds();
            logger.info("获取激活的RSS源: count={}", feeds.size());
            return ApiResponse.success(feeds);
        } catch (Exception e) {
            logger.error("获取激活的RSS源失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取RSS源
     */
    @GetMapping("/{id}")
    public ApiResponse<RssFeed> getFeedById(@PathVariable Long id) {
        try {
            return rssFeedService.getFeedById(id)
                    .map(feed -> ApiResponse.success(feed))
                    .orElse(ApiResponse.error("RSS源不存在: id=" + id));
        } catch (Exception e) {
            logger.error("获取RSS源失败: id={}, error={}", id, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 根据分类获取RSS源
     */
    @GetMapping("/category/{category}")
    public ApiResponse<List<RssFeed>> getFeedsByCategory(@PathVariable String category) {
        try {
            List<RssFeed> feeds = rssFeedService.getFeedsByCategory(category);
            logger.info("获取分类 {} 的RSS源: count={}", category, feeds.size());
            return ApiResponse.success(feeds);
        } catch (Exception e) {
            logger.error("获取分类RSS源失败: category={}, error={}", category, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 搜索RSS源
     */
    @GetMapping("/search")
    public ApiResponse<List<RssFeed>> searchFeeds(@RequestParam String keyword) {
        try {
            List<RssFeed> feeds = rssFeedService.searchFeeds(keyword);
            logger.info("搜索RSS源: keyword={}, count={}", keyword, feeds.size());
            return ApiResponse.success(feeds);
        } catch (Exception e) {
            logger.error("搜索RSS源失败: keyword={}, error={}", keyword, e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public ApiResponse<List<String>> getAllCategories() {
        try {
            List<String> categories = rssFeedService.getAllCategories();
            logger.info("获取RSS分类: count={}", categories.size());
            return ApiResponse.success(categories);
        } catch (Exception e) {
            logger.error("获取RSS分类失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 添加RSS源（需要登录）
     */
    @PostMapping
    public ApiResponse<RssFeed> addFeed(@Valid @RequestBody AddRssFeedRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 添加RSS源: url={}", user.getId(), request.getUrl());

            RssFeed feed = new RssFeed();
            feed.setUrl(request.getUrl());
            feed.setTitle(request.getTitle());
            feed.setDescription(request.getDescription());
            feed.setCategory(request.getCategory());
            feed.setLanguage(request.getLanguage());
            feed.setIconUrl(request.getIconUrl());
            feed.setIsActive(true);

            RssFeed savedFeed = rssFeedService.addFeed(feed);

            logger.info("RSS源添加成功: id={}, title={}", savedFeed.getId(), savedFeed.getTitle());
            return ApiResponse.success("RSS源添加成功", savedFeed);

        } catch (IllegalArgumentException e) {
            logger.warn("添加RSS源失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("添加RSS源异常: {}", e.getMessage());
            return ApiResponse.error("添加RSS源失败: " + e.getMessage());
        }
    }

    /**
     * 更新RSS源（需要登录）
     */
    @PutMapping("/{id}")
    public ApiResponse<RssFeed> updateFeed(@PathVariable Long id, @RequestBody RssFeed feedDetails, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 更新RSS源: id={}", user.getId(), id);

            RssFeed updatedFeed = rssFeedService.updateFeed(id, feedDetails);

            logger.info("RSS源更新成功: id={}", id);
            return ApiResponse.success("RSS源更新成功", updatedFeed);

        } catch (IllegalArgumentException e) {
            logger.warn("更新RSS源失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("更新RSS源异常: {}", e.getMessage());
            return ApiResponse.error("更新RSS源失败: " + e.getMessage());
        }
    }

    /**
     * 删除RSS源（需要登录）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFeed(@PathVariable Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 删除RSS源: id={}", user.getId(), id);

            rssFeedService.deleteFeed(id);

            logger.info("RSS源删除成功: id={}", id);
            return ApiResponse.success("RSS源删除成功", null);

        } catch (IllegalArgumentException e) {
            logger.warn("删除RSS源失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("删除RSS源异常: {}", e.getMessage());
            return ApiResponse.error("删除RSS源失败: " + e.getMessage());
        }
    }

    /**
     * 激活/禁用RSS源（需要登录）
     */
    @PatchMapping("/{id}/toggle")
    public ApiResponse<Void> toggleFeedActive(@PathVariable Long id, @RequestParam boolean active, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} {}RSS源: id={}", user.getId(), active ? "激活" : "禁用", id);

            rssFeedService.toggleFeedActive(id, active);

            logger.info("RSS源状态更新成功: id={}, active={}", id, active);
            return ApiResponse.success("RSS源状态更新成功", null);

        } catch (IllegalArgumentException e) {
            logger.warn("更新RSS源状态失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("更新RSS源状态异常: {}", e.getMessage());
            return ApiResponse.error("更新RSS源状态失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发RSS源抓取（需要登录）
     */
    @PostMapping("/{id}/fetch")
    public ApiResponse<Integer> fetchFeed(@PathVariable Long id, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 手动抓取RSS源: id={}", user.getId(), id);

            int count = rssFeedService.fetchFeed(id);

            logger.info("RSS源抓取完成: id={}, newArticles={}", id, count);
            return ApiResponse.success("抓取完成，新增 " + count + " 篇文章", count);

        } catch (Exception e) {
            logger.error("手动抓取RSS源异常: {}", e.getMessage());
            return ApiResponse.error("抓取失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发所有RSS源抓取（需要登录）
     */
    @PostMapping("/fetch-all")
    public ApiResponse<Integer> fetchAllFeeds(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 手动抓取所有RSS源", user.getId());

            int count = rssFeedService.fetchAllFeeds();

            logger.info("所有RSS源抓取完成: totalNewArticles={}", count);
            return ApiResponse.success("抓取完成，共新增 " + count + " 篇文章", count);

        } catch (Exception e) {
            logger.error("手动抓取所有RSS源异常: {}", e.getMessage());
            return ApiResponse.error("抓取失败: " + e.getMessage());
        }
    }

    /**
     * 清除缓存（需要登录）
     */
    @PostMapping("/clear-cache")
    public ApiResponse<Void> clearCache(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 清除RSS源缓存", user.getId());

            rssFeedService.clearCache();

            logger.info("RSS源缓存清除成功");
            return ApiResponse.success("缓存清除成功", null);

        } catch (Exception e) {
            logger.error("清除缓存异常: {}", e.getMessage());
            return ApiResponse.error("清除缓存失败: " + e.getMessage());
        }
    }

    // ==================== 用户订阅相关接口 ====================

    /**
     * 获取用户的订阅列表
     */
    @GetMapping("/subscriptions")
    public ApiResponse<List<RssFeed>> getUserSubscriptions(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("获取用户 {} 的订阅列表", user.getId());

            List<RssFeed> subscriptions = subscriptionService.getUserSubscriptionsWithFeeds(user.getId());

            logger.info("用户 {} 有 {} 个订阅", user.getId(), subscriptions.size());
            return ApiResponse.success(subscriptions);

        } catch (Exception e) {
            logger.error("获取订阅列表失败: {}", e.getMessage());
            return ApiResponse.error("获取订阅列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查用户是否订阅了指定RSS源
     */
    @GetMapping("/{feedId}/subscription/status")
    public ApiResponse<Boolean> checkSubscription(@PathVariable Long feedId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            boolean isSubscribed = subscriptionService.isSubscribed(user.getId(), feedId);

            return ApiResponse.success(isSubscribed);

        } catch (Exception e) {
            logger.error("检查订阅状态失败: {}", e.getMessage());
            return ApiResponse.error("检查订阅状态失败: " + e.getMessage());
        }
    }

    /**
     * 订阅RSS源
     */
    @PostMapping("/{feedId}/subscribe")
    public ApiResponse<UserRssSubscription> subscribe(
            @PathVariable Long feedId,
            @RequestParam(required = false) String customTitle,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 订阅RSS源: feedId={}", user.getId(), feedId);

            UserRssSubscription subscription = subscriptionService.subscribe(user.getId(), feedId, customTitle);

            logger.info("订阅成功: id={}", subscription.getId());
            return ApiResponse.success("订阅成功", subscription);

        } catch (IllegalArgumentException e) {
            logger.warn("订阅失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("订阅异常: {}", e.getMessage());
            return ApiResponse.error("订阅失败: " + e.getMessage());
        }
    }

    /**
     * 取消订阅RSS源
     */
    @DeleteMapping("/{feedId}/subscribe")
    public ApiResponse<Void> unsubscribe(@PathVariable Long feedId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 取消订阅RSS源: feedId={}", user.getId(), feedId);

            subscriptionService.unsubscribe(user.getId(), feedId);

            logger.info("取消订阅成功: userId={}, feedId={}", user.getId(), feedId);
            return ApiResponse.success("取消订阅成功", null);

        } catch (IllegalArgumentException e) {
            logger.warn("取消订阅失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("取消订阅异常: {}", e.getMessage());
            return ApiResponse.error("取消订阅失败: " + e.getMessage());
        }
    }

    /**
     * 切换收藏状态
     */
    @PatchMapping("/{feedId}/favorite")
    public ApiResponse<Void> toggleFavorite(@PathVariable Long feedId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 切换收藏状态: feedId={}", user.getId(), feedId);

            subscriptionService.toggleFavorite(user.getId(), feedId);

            logger.info("收藏状态切换成功");
            return ApiResponse.success("收藏状态更新成功", null);

        } catch (IllegalArgumentException e) {
            logger.warn("切换收藏状态失败: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("切换收藏状态异常: {}", e.getMessage());
            return ApiResponse.error("切换收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏的RSS源
     */
    @GetMapping("/subscriptions/favorites")
    public ApiResponse<List<RssFeed>> getUserFavorites(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("获取用户 {} 的收藏列表", user.getId());

            List<RssFeed> favorites = subscriptionService.getUserFavoriteFeeds(user.getId());

            logger.info("用户 {} 有 {} 个收藏", user.getId(), favorites.size());
            return ApiResponse.success(favorites);

        } catch (Exception e) {
            logger.error("获取收藏列表失败: {}", e.getMessage());
            return ApiResponse.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新订阅优先级
     */
    @PatchMapping("/{feedId}/subscription/priority")
    public ApiResponse<Void> updatePriority(
            @PathVariable Long feedId,
            @RequestParam Integer priority,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 更新订阅优先级: feedId={}, priority={}", user.getId(), feedId, priority);

            subscriptionService.updatePriority(user.getId(), feedId, priority);

            return ApiResponse.success("优先级更新成功", null);

        } catch (Exception e) {
            logger.error("更新优先级失败: {}", e.getMessage());
            return ApiResponse.error("更新优先级失败: " + e.getMessage());
        }
    }

    /**
     * 更新自定义标题
     */
    @PatchMapping("/{feedId}/subscription/title")
    public ApiResponse<Void> updateCustomTitle(
            @PathVariable Long feedId,
            @RequestParam String customTitle,
            HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ApiResponse.error("请先登录");
            }

            logger.info("用户 {} 更新自定义标题: feedId={}, title={}", user.getId(), feedId, customTitle);

            subscriptionService.updateCustomTitle(user.getId(), feedId, customTitle);

            return ApiResponse.success("自定义标题更新成功", null);

        } catch (Exception e) {
            logger.error("更新自定义标题失败: {}", e.getMessage());
            return ApiResponse.error("更新自定义标题失败: " + e.getMessage());
        }
    }
}
