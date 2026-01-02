package com.newsapp.service;

import com.newsapp.dto.ChangePasswordRequest;
import com.newsapp.dto.LoginRequest;
import com.newsapp.dto.RegisterRequest;
import com.newsapp.dto.UpdateProfileRequest;
import com.newsapp.dto.UserProfileResponse;
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

    /**
     * 获取用户资料
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = getUserById(userId);
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatarUrl());
        response.setBio(user.getBio());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    /**
     * 更新用户资料
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("更新用户资料: userId={}", userId);

        User user = getUserById(userId);

        // 检查用户名是否被占用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        // 检查邮箱是否被占用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }

        // 更新简介
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        User savedUser = userRepository.save(user);
        log.info("用户资料更新成功: userId={}", userId);
        return savedUser;
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("修改密码: userId={}", userId);

        User user = getUserById(userId);

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("密码修改成功: userId={}", userId);
    }

    /**
     * 更新头像URL
     */
    @Transactional
    public void updateAvatarUrl(Long userId, String avatarUrl) {
        log.info("更新头像: userId={}, avatarUrl={}", userId, avatarUrl);
        User user = getUserById(userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    /**
     * 删除用户（账户注销）
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("删除用户: userId={}", userId);
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(userId);
        log.info("用户删除成功: userId={}", userId);
    }
}
