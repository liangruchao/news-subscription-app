package com.newsapp.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * CSRF Token 控制器
 * 提供前端获取 CSRF Token 的接口
 */
@RestController
@RequestMapping("/api")
public class CsrfController {

    /**
     * 获取 CSRF Token
     * 前端在发送 POST/PUT/DELETE 请求前，需要先调用此接口获取 Token
     */
    @GetMapping("/csrf")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        return token;
    }
}
