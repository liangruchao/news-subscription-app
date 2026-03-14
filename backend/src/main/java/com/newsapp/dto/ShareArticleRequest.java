package com.newsapp.dto;

/**
 * 分享文章请求DTO
 */
public class ShareArticleRequest {
    private Long articleId;
    private String title;  // 自定义标题（可选）
    private String description;  // 自定义描述（可选）
    private Integer expireDays;  // 过期天数（可选，默认7天）

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExpireDays() {
        return expireDays;
    }

    public void setExpireDays(Integer expireDays) {
        this.expireDays = expireDays;
    }
}
