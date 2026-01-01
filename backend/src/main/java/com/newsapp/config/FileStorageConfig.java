package com.newsapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 文件存储配置
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.avatar-dir:./uploads/avatars}")
    private String avatarDir;

    public String getUploadDir() {
        return uploadDir;
    }

    public String getAvatarDir() {
        return avatarDir;
    }

    /**
     * 初始化上传目录
     */
    public void initDirectories() {
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        File avatarDirectory = new File(avatarDir);
        if (!avatarDirectory.exists()) {
            avatarDirectory.mkdirs();
        }
    }

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射头像访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
