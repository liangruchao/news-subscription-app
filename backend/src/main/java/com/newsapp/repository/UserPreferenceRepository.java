package com.newsapp.repository;

import com.newsapp.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户偏好 Repository
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    /**
     * 根据用户ID查找偏好设置
     */
    Optional<UserPreference> findByUserId(Long userId);

    /**
     * 检查用户是否已有偏好设置
     */
    boolean existsByUserId(Long userId);
}
