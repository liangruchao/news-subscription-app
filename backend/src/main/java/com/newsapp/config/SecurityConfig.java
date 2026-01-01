package com.newsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * 简化版：放行所有请求，使用 Session 管理
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 放行所有请求
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 禁用 CSRF（简化版）
            .csrf(csrf -> csrf.disable())
            // 允许 iframe（如果需要）
            .headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }
}
