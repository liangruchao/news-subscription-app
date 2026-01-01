package com.newsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Spring Security 配置
 * VPS 部署环境：禁用 CSRF 保护，简化 API 访问
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 默认安全配置（适用于 VPS 部署）
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 放行所有请求
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 禁用 CSRF 保护（VPS 部署环境）
            .csrf(csrf -> csrf.disable())
            // 配置 Session 管理
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            // 配置安全响应头
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            );

        return http.build();
    }

    /**
     * 测试环境安全配置 - 禁用 CSRF
     */
    @Bean
    @Profile("test")
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().sameOrigin());
        return http.build();
    }

    /**
     * 密码编码器 - 使用 BCrypt 加密算法
     * 强度设置为 10，是安全性和性能的平衡点
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
