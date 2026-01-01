package com.newsapp.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GlobalExceptionHandler 集成测试
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("GlobalExceptionHandler 集成测试")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("输入验证异常处理")
    class ValidationExceptionTests {

        @Test
        @DisplayName("处理用户名为空的验证错误")
        void handleValidationException_emptyUsername() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("");
            request.setEmail("test@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("用户名")));
        }

        @Test
        @DisplayName("处理邮箱格式错误的验证错误")
        void handleValidationException_invalidEmail() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("invalid-email");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("邮箱")));
        }

        @Test
        @DisplayName("处理密码强度不够的验证错误")
        void handleValidationException_weakPassword() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("weak");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("密码")));
        }
    }

    @Nested
    @DisplayName("业务异常处理")
    class BusinessExceptionTests {

        @Test
        @DisplayName("处理用户名已存在的业务异常")
        void handleBusinessException_usernameExists() throws Exception {
            // 先注册一个用户
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existinguser");
            request.setEmail("user1@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            // 尝试注册相同用户名
            request.setEmail("user2@example.com");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
        }

        @Test
        @DisplayName("处理登录失败的业务异常")
        void handleBusinessException_loginFailure() throws Exception {
            String loginRequest = "{\"username\":\"nonexistent\",\"password\":\"Password123\"}";

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }
    }

    @Nested
    @DisplayName("运行时异常处理")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("处理未登录时的运行时异常")
        void handleRuntimeException_notLoggedIn() throws Exception {
            mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("请先登录")));
        }
    }
}
