package com.newsapp.repository;

import com.newsapp.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 订阅 Repository
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 查找用户的所有订阅
     */
    List<Subscription> findByUserId(Long userId);

    /**
     * 查找用户对特定类别的订阅
     */
    Optional<Subscription> findByUserIdAndCategory(Long userId, String category);

    /**
     * 检查用户是否已订阅某类别
     */
    boolean existsByUserIdAndCategory(Long userId, String category);

    /**
     * 删除用户对特定类别的订阅
     */
    void deleteByUserIdAndCategory(Long userId, String category);

    /**
     * 统计用户的订阅数量
     */
    long countByUserId(Long userId);
}
