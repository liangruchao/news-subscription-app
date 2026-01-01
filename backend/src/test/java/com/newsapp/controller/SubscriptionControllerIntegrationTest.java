package com.newsapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.dto.SubscriptionRequest;
import com.newsapp.repository.SubscriptionRepository;
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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SubscriptionController 集成测试
 */
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Transactional
@DisplayName("SubscriptionController 集成测试")
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        // 清理数据库
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();

        // 注册并登录用户
        session = new MockHttpSession();
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
                .session(session))
            .andExpect(status().isOk());
    }

    @Nested
    @DisplayName("获取订阅列表接口")
    class GetSubscriptionsTests {

        @Test
        @DisplayName("获取订阅列表 - 有订阅")
        void getSubscriptions_withSubscriptions() throws Exception {
            // 添加订阅
            SubscriptionRequest request = new SubscriptionRequest();
            request.setCategory("technology");
            mockMvc.perform(post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session))
                .andExpect(status().isOk());

            // 获取订阅列表
            mockMvc.perform(get("/api/subscriptions")
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].category").value("technology"));
        }

        @Test
        @DisplayName("获取订阅列表 - 无订阅")
        void getSubscriptions_noSubscriptions() throws Exception {
            mockMvc.perform(get("/api/subscriptions")
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("获取订阅列表 - 未登录")
        void getSubscriptions_notLoggedIn() throws Exception {
            MockHttpSession newSession = new MockHttpSession();
            mockMvc.perform(get("/api/subscriptions")
                    .session(newSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先登录"));
        }
    }

    @Nested
    @DisplayName("添加订阅接口")
    class AddSubscriptionTests {

        @Test
        @DisplayName("添加订阅成功")
        void addSubscription_success() throws Exception {
            SubscriptionRequest request = new SubscriptionRequest();
            request.setCategory("technology");

            mockMvc.perform(post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("订阅成功"));
        }

        @Test
        @DisplayName("添加订阅失败 - 已订阅")
        void addSubscription_alreadySubscribed() throws Exception {
            // 第一次订阅
            SubscriptionRequest request = new SubscriptionRequest();
            request.setCategory("technology");

            mockMvc.perform(post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session))
                .andExpect(status().isOk());

            // 第二次订阅相同类别
            mockMvc.perform(post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("您已订阅该类别"));
        }

        @Test
        @DisplayName("添加订阅成功 - 所有有效类别")
        void addSubscription_allValidCategories() throws Exception {
            String[] categories = {"business", "entertainment", "general",
                                  "health", "science", "sports", "technology"};

            for (String category : categories) {
                SubscriptionRequest request = new SubscriptionRequest();
                request.setCategory(category);

                mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("添加订阅失败 - 无效类别")
        void addSubscription_invalidCategory() throws Exception {
            SubscriptionRequest request = new SubscriptionRequest();
            request.setCategory("invalid");

            mockMvc.perform(post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("取消订阅接口")
    class RemoveSubscriptionTests {

        @Test
        @DisplayName("取消订阅成功")
        void removeSubscription_success() throws Exception {
            // 先添加订阅
            SubscriptionRequest request = new SubscriptionRequest();
            request.setCategory("technology");
            mockMvc.perform(post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .session(session))
                .andExpect(status().isOk());

            // 取消订阅
            mockMvc.perform(delete("/api/subscriptions/technology")
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("取消订阅成功"));
        }

        @Test
        @DisplayName("取消订阅失败 - 未订阅")
        void removeSubscription_notSubscribed() throws Exception {
            mockMvc.perform(delete("/api/subscriptions/technology")
                    .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("您未订阅该类别"));
        }
    }
}
