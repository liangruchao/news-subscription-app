package com.newsapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.dto.LoginRequest;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试
 * 使用 MockMvc 测试完整的 HTTP 请求/响应流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("AuthController 集成测试")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 清理数据库
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("用户注册接口")
    class RegisterEndpointTests {

        @Test
        @DisplayName("注册成功 - 返回用户信息")
        void registerSuccess() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("newuser@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.id").exists());

            // 验证密码被加密
            mockMvc.perform(get("/api/auth/current"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void registerFail_usernameExists() throws Exception {
            // 先注册一个用户
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existing");
            request.setEmail("user1@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            // 尝试用相同用户名注册
            request.setEmail("user2@example.com");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
        }

        @Test
        @DisplayName("注册失败 - 邮箱已存在")
        void registerFail_emailExists() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("user1");
            request.setEmail("existing@example.com");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

            request.setUsername("user2");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("邮箱已被注册"));
        }

        @Test
        @DisplayName("注册失败 - 密码不符合强度要求")
        void registerFail_weakPassword() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setEmail("test@example.com");
            request.setPassword("weak"); // 不符合强度要求

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("用户登录接口")
    class LoginEndpointTests {

        @Test
        @DisplayName("登录成功 - 正确凭据")
        void loginSuccess() throws Exception {
            // 先注册用户
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("testuser");
            registerRequest.setEmail("test@example.com");
            registerRequest.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

            // 登录
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("Password123");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.username").value("testuser"));
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void loginFail_userNotFound() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword("Password123");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void loginFail_wrongPassword() throws Exception {
            // 注册用户
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("testuser");
            registerRequest.setEmail("test@example.com");
            registerRequest.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

            // 用错误密码登录
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("testuser");
            loginRequest.setPassword("WrongPassword");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }
    }

    @Nested
    @DisplayName("当前用户接口")
    class CurrentUserTests {

        @Test
        @DisplayName("获取当前用户 - 未登录")
        void getCurrentUser_notLoggedIn() throws Exception {
            mockMvc.perform(get("/api/auth/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("未登录"));
        }

        @Test
        @DisplayName("获取当前用户 - 已登录")
        void getCurrentUser_loggedIn() throws Exception {
            // 使用 MockHttpSession 注册并登录
            MockHttpSession session = new MockHttpSession();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("testuser");
            registerRequest.setEmail("test@example.com");
            registerRequest.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
                    .session(session))
                .andExpect(status().isOk());

            // 检查当前用户
            mockMvc.perform(get("/api/auth/current")
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("testuser"));
        }
    }

    @Nested
    @DisplayName("登出接口")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void logoutSuccess() throws Exception {
            MockHttpSession session = new MockHttpSession();

            // 注册
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("testuser");
            registerRequest.setEmail("test@example.com");
            registerRequest.setPassword("Password123");

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest))
                    .session(session))
                .andExpect(status().isOk());

            // 登出
            mockMvc.perform(post("/api/auth/logout")
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));

            // 验证登出后无法获取用户
            mockMvc.perform(get("/api/auth/current")
                    .session(session))
                .andExpect(jsonPath("$.success").value(false));
        }
    }
}
