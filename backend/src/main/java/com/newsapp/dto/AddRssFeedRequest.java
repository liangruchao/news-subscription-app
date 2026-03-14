package com.newsapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * 添加RSS源请求DTO
 */
public class AddRssFeedRequest {

    @NotBlank(message = "RSS源URL不能为空")
    @URL(message = "RSS源URL格式不正确")
    private String url;

    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Size(max = 100, message = "分类长度不能超过100个字符")
    private String category;

    @Size(max = 10, message = "语言代码长度不能超过10个字符")
    private String language;

    @Size(max = 500, message = "图标URL长度不能超过500个字符")
    @URL(message = "图标URL格式不正确")
    private String iconUrl;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
