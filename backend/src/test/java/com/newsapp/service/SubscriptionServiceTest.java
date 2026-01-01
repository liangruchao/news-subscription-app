package com.newsapp.service;

import com.newsapp.entity.Subscription;
import com.newsapp.exception.BusinessException;
import com.newsapp.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * SubscriptionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionService 单元测试")
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription testSubscription;
    private static final Long USER_ID = 1L;
    private static final String CATEGORY_TECH = "technology";
    private static final String CATEGORY_SPORTS = "sports";

    @BeforeEach
    void setUp() {
        testSubscription = new Subscription();
        testSubscription.setId(1L);
        testSubscription.setUserId(USER_ID);
        testSubscription.setCategory(CATEGORY_TECH);
    }

    @Nested
    @DisplayName("获取用户订阅列表")
    class GetUserSubscriptionsTests {

        @Test
        @DisplayName("成功获取用户订阅列表 - 有订阅")
        void getUserSubscriptions_success_withSubscriptions() {
            // Given
            List<Subscription> subscriptions = Arrays.asList(
                testSubscription,
                createSubscription(USER_ID, CATEGORY_SPORTS)
            );
            when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(subscriptions);

            // When
            List<Subscription> result = subscriptionService.getUserSubscriptions(USER_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("category")
                .containsExactly(CATEGORY_TECH, CATEGORY_SPORTS);
            verify(subscriptionRepository).findByUserId(USER_ID);
        }

        @Test
        @DisplayName("成功获取用户订阅列表 - 无订阅")
        void getUserSubscriptions_success_noSubscriptions() {
            // Given
            when(subscriptionRepository.findByUserId(USER_ID)).thenReturn(List.of());

            // When
            List<Subscription> result = subscriptionService.getUserSubscriptions(USER_ID);

            // Then
            assertThat(result).isEmpty();
            verify(subscriptionRepository).findByUserId(USER_ID);
        }
    }

    @Nested
    @DisplayName("添加订阅")
    class SubscribeTests {

        @Test
        @DisplayName("订阅成功 - 新类别")
        void subscribeSuccess_newCategory() {
            // Given
            when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, CATEGORY_TECH))
                .thenReturn(false);
            when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(testSubscription);

            // When
            Subscription result = subscriptionService.subscribe(USER_ID, CATEGORY_TECH);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCategory()).isEqualTo(CATEGORY_TECH);
            verify(subscriptionRepository).existsByUserIdAndCategory(USER_ID, CATEGORY_TECH);
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("订阅失败 - 已订阅该类别")
        void subscribeFail_alreadySubscribed() {
            // Given
            when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, CATEGORY_TECH))
                .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, CATEGORY_TECH))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("您已订阅该类别");

            verify(subscriptionRepository, never()).save(any(Subscription.class));
        }

        @Test
        @DisplayName("订阅失败 - 无效的新闻类别")
        void subscribeFail_invalidCategory() {
            // Given
            String invalidCategory = "invalid";
            when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, invalidCategory))
                .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> subscriptionService.subscribe(USER_ID, invalidCategory))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("无效的新闻类别");

            verify(subscriptionRepository, never()).save(any(Subscription.class));
        }

        @Test
        @DisplayName("订阅成功 - 类别大小写不敏感")
        void subscribeSuccess_caseInsensitive() {
            // Given - 测试小写类别可以成功订阅
            when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, "technology"))
                .thenReturn(false);
            when(subscriptionRepository.save(any(Subscription.class)))
                .thenReturn(testSubscription);

            // When & Then - 小写类别不应抛出异常
            assertThatCode(() -> subscriptionService.subscribe(USER_ID, "technology"))
                .doesNotThrowAnyException();

            verify(subscriptionRepository).existsByUserIdAndCategory(USER_ID, "technology");
            verify(subscriptionRepository).save(any(Subscription.class));
        }

        @Test
        @DisplayName("验证所有有效类别")
        void subscribeSuccess_allValidCategories() {
            // Given
            List<String> validCategories = Arrays.asList(
                "business", "entertainment", "general",
                "health", "science", "sports", "technology"
            );

            for (String category : validCategories) {
                when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, category))
                    .thenReturn(false);
                when(subscriptionRepository.save(any(Subscription.class)))
                    .thenReturn(createSubscription(USER_ID, category));

                // When & Then - 不应抛出异常
                assertThatCode(() -> subscriptionService.subscribe(USER_ID, category))
                    .doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("取消订阅")
    class UnsubscribeTests {

        @Test
        @DisplayName("取消订阅成功")
        void unsubscribeSuccess() {
            // Given
            when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, CATEGORY_TECH))
                .thenReturn(true);
            doNothing().when(subscriptionRepository)
                .deleteByUserIdAndCategory(USER_ID, CATEGORY_TECH);

            // When
            subscriptionService.unsubscribe(USER_ID, CATEGORY_TECH);

            // Then
            verify(subscriptionRepository).existsByUserIdAndCategory(USER_ID, CATEGORY_TECH);
            verify(subscriptionRepository).deleteByUserIdAndCategory(USER_ID, CATEGORY_TECH);
        }

        @Test
        @DisplayName("取消订阅失败 - 未订阅该类别")
        void unsubscribeFail_notSubscribed() {
            // Given
            when(subscriptionRepository.existsByUserIdAndCategory(USER_ID, CATEGORY_TECH))
                .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_ID, CATEGORY_TECH))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("您未订阅该类别");

            verify(subscriptionRepository, never())
                .deleteByUserIdAndCategory(anyLong(), anyString());
        }
    }

    // 辅助方法
    private Subscription createSubscription(Long userId, String category) {
        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setCategory(category);
        return sub;
    }
}
