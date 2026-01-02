package com.newsapp.service;

import com.newsapp.dto.LoginHistoryResponse;
import com.newsapp.entity.LoginHistory;
import com.newsapp.repository.LoginHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录历史服务
 */
@Service
public class LoginHistoryService {

    private static final Logger log = LoggerFactory.getLogger(LoginHistoryService.class);

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    /**
     * 记录登录
     */
    @Transactional
    public LoginHistory recordLogin(Long userId, HttpServletRequest request) {
        LoginHistory history = new LoginHistory();
        history.setUserId(userId);
        history.setIpAddress(getClientIpAddress(request));
        history.setUserAgent(request.getHeader("User-Agent"));

        LoginHistory saved = loginHistoryRepository.save(history);
        log.info("记录登录历史: userId={}, ip={}", userId, history.getIpAddress());
        return saved;
    }

    /**
     * 记录登出
     */
    @Transactional
    public void recordLogout(Long historyId) {
        loginHistoryRepository.findById(historyId).ifPresent(history -> {
            history.setLogoutTime(LocalDateTime.now());
            loginHistoryRepository.save(history);
            log.info("记录登出历史: historyId={}", historyId);
        });
    }

    /**
     * 获取用户最近登录记录
     */
    public List<LoginHistoryResponse> getRecentLogins(Long userId, int limit) {
        List<LoginHistory> histories = loginHistoryRepository
                .findByUserIdOrderByLoginTimeDesc(userId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        return histories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 转换为响应对象
     */
    private LoginHistoryResponse toResponse(LoginHistory history) {
        LoginHistoryResponse response = new LoginHistoryResponse();
        response.setId(history.getId());
        response.setIpAddress(history.getIpAddress());
        response.setUserAgent(history.getUserAgent());
        response.setLoginTime(history.getLoginTime());
        response.setLogoutTime(history.getLogoutTime());
        return response;
    }
}
