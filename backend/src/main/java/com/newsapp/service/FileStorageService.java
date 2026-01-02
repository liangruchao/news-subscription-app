package com.newsapp.service;

import com.newsapp.config.FileStorageConfig;
import com.newsapp.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件存储服务
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    private final FileStorageConfig fileStorageConfig;

    public FileStorageService(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
        // 初始化目录
        fileStorageConfig.initDirectories();
    }

    /**
     * 存储头像文件
     */
    public String storeAvatar(MultipartFile file, Long userId) {
        log.info("开始存储头像: userId={}, fileName={}", userId, file.getOriginalFilename());

        // 验证文件
        validateFile(file);

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String newFilename = userId + "_" + UUID.randomUUID() + extension;

            // 存储文件
            Path targetPath = Paths.get(fileStorageConfig.getAvatarDir(), newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回访问URL
            String fileUrl = "/uploads/avatars/" + newFilename;
            log.info("头像存储成功: userId={}, fileUrl={}", userId, fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("头像存储失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException("头像上传失败");
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过5MB");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        boolean isValidExtension = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                isValidExtension = true;
                break;
            }
        }

        if (!isValidExtension) {
            throw new BusinessException("只支持 jpg、jpeg、png、gif 格式的图片");
        }

        // 检查Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("文件必须是图片类型");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * 删除文件
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
                String filePath = fileStorageConfig.getUploadDir() + fileUrl.substring("/uploads".length());
                Path path = Paths.get(filePath);
                Files.deleteIfExists(path);
                log.info("文件删除成功: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}, error={}", fileUrl, e.getMessage());
        }
    }
}
