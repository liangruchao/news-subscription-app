package com.newsapp.service;

import com.newsapp.dto.LoginRequest;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.entity.User;
import com.newsapp.exception.BusinessException;
import com.newsapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 * 使用 Mockito 模拟依赖，测试业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$hashedPassword");
    }

    @Nested
    @DisplayName("用户注册测试")
    class RegisterTests {

        @Test
        @DisplayName("注册成功 - 新用户")
        void registerSuccess_newUser() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User result = userService.register(registerRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");

            verify(userRepository).existsByUsername("testuser");
            verify(userRepository).existsByEmail("test@example.com");
            verify(passwordEncoder).encode("Password123");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("注册失败 - 用户名已存在")
        void registerFail_usernameExists() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名已存在");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("注册失败 - 邮箱已存在")
        void registerFail_emailExists() {
            // Given
            when(userRepository.existsByUsername("testuser")).thenReturn(false);
            when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("邮箱已被注册");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("注册成功 - 密码被加密")
        void registerSuccess_passwordIsEncoded() {
            // Given
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("Password123")).thenReturn("$2a$10$encodedHash");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getPassword()).isEqualTo("$2a$10$encodedHash");
                return user;
            });

            // When
            userService.register(registerRequest);

            // Then
            verify(passwordEncoder).encode("Password123");
        }
    }

    @Nested
    @DisplayName("用户登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功 - 正确的用户名和密码")
        void loginSuccess_validCredentials() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password123", testUser.getPassword())).thenReturn(true);

            // When
            User result = userService.login(loginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");

            verify(userRepository).findByUsername("testuser");
            verify(passwordEncoder).matches("Password123", testUser.getPassword());
        }

        @Test
        @DisplayName("登录失败 - 用户不存在")
        void loginFail_userNotFound() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("登录失败 - 密码错误")
        void loginFail_wrongPassword() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password123", testUser.getPassword())).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户名或密码错误");
        }

        @Test
        @DisplayName("登录成功 - 密码匹配使用BCrypt")
        void loginSuccess_usesBcryptMatching() {
            // Given
            String hashedPassword = "$2a$10$ABCDEFGHIJKLMNOPQRSTUVWXYZ123456";
            testUser.setPassword(hashedPassword);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("Password123", hashedPassword)).thenReturn(true);

            // When
            userService.login(loginRequest);

            // Then
            verify(passwordEncoder).matches("Password123", hashedPassword);
        }
    }
}
