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
 * 启用 CSRF 保护，使用 Cookie 存储 CSRF Token
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 生产环境安全配置
     */
    @Bean
    @Profile("!test")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 放行所有请求（使用 Session 认证）
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            // 启用 CSRF 保护
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // 使用标准处理器，接受请求头中的 X-XSRF-TOKEN
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            // 配置 Session 管理
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            // 配置安全响应头
            .headers(headers -> headers
                .frameOptions().sameOrigin()
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                )
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
