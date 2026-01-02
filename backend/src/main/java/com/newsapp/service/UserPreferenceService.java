package com.newsapp.service;

import com.newsapp.dto.UpdatePreferenceRequest;
import com.newsapp.entity.UserPreference;
import com.newsapp.exception.BusinessException;
import com.newsapp.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户偏好服务
 */
@Service
public class UserPreferenceService {

    private static final Logger log = LoggerFactory.getLogger(UserPreferenceService.class);

    private final UserPreferenceRepository preferenceRepository;

    public UserPreferenceService(UserPreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * 获取用户偏好设置
     */
    public UserPreference getUserPreference(Long userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreference(userId));
    }

    /**
     * 创建默认偏好设置
     */
    @Transactional
    public UserPreference createDefaultPreference(Long userId) {
        log.info("创建默认偏好设置: userId={}", userId);
        UserPreference preference = new UserPreference();
        preference.setUserId(userId);
        return preferenceRepository.save(preference);
    }

    /**
     * 更新用户偏好设置
     */
    @Transactional
    public UserPreference updatePreference(Long userId, UpdatePreferenceRequest request) {
        log.info("更新用户偏好设置: userId={}", userId);

        UserPreference preference = getUserPreference(userId);

        // 更新通知偏好
        if (request.getEmailNotification() != null) {
            preference.setEmailNotification(request.getEmailNotification());
        }
        if (request.getDailyDigest() != null) {
            preference.setDailyDigest(request.getDailyDigest());
        }
        if (request.getSubscriptionUpdate() != null) {
            preference.setSubscriptionUpdate(request.getSubscriptionUpdate());
        }

        // 更新界面偏好
        if (request.getTheme() != null) {
            if (!request.getTheme().matches("light|dark|auto")) {
                throw new BusinessException("主题必须是 light、dark 或 auto");
            }
            preference.setTheme(request.getTheme());
        }
        if (request.getLanguage() != null) {
            preference.setLanguage(request.getLanguage());
        }
        if (request.getPageSize() != null) {
            if (request.getPageSize() < 5 || request.getPageSize() > 50) {
                throw new BusinessException("每页条数必须在5-50之间");
            }
            preference.setPageSize(request.getPageSize());
        }

        UserPreference saved = preferenceRepository.save(preference);
        log.info("用户偏好设置更新成功: userId={}", userId);
        return saved;
    }

    /**
     * 删除用户偏好设置
     */
    @Transactional
    public void deleteUserPreference(Long userId) {
        preferenceRepository.findByUserId(userId).ifPresent(preference -> {
            preferenceRepository.delete(preference);
            log.info("删除用户偏好设置: userId={}", userId);
        });
    }
}
