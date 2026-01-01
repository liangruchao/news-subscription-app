package com.newsapp.service;

import com.newsapp.dto.LoginRequest;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.entity.User;
import com.newsapp.exception.BusinessException;
import com.newsapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户注册
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("用户注册请求: username={}", request.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("注册失败: 用户名已存在 - {}", request.getUsername());
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("注册失败: 邮箱已被注册 - {}", request.getEmail());
            throw new BusinessException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // 使用 BCrypt 加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("用户注册成功: id={}, username={}", savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    /**
     * 用户登录
     */
    public User login(LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("登录失败: 用户不存在 - {}", request.getUsername());
                    return new BusinessException("用户名或密码错误");
                });

        // 使用 BCrypt 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误 - {}", request.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        log.info("用户登录成功: id={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    /**
     * 根据 ID 获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
