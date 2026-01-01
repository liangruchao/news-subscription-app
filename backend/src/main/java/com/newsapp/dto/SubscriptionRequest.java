package com.newsapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 订阅请求
 */
public class SubscriptionRequest {

    @NotBlank(message = "新闻类别不能为空")
    @Pattern(regexp = "^(business|entertainment|general|health|science|sports|technology)$",
            message = "新闻类别必须是以下之一: business, entertainment, general, health, science, sports, technology")
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
