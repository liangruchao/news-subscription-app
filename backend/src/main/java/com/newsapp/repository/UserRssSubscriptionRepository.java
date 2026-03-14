package com.newsapp.repository;

import com.newsapp.entity.UserRssSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户RSS订阅 Repository
 */
@Repository
public interface UserRssSubscriptionRepository extends JpaRepository<UserRssSubscription, Long> {

    /**
     * 查找用户是否订阅了指定RSS源
     */
    Optional<UserRssSubscription> findByUserIdAndFeedId(Long userId, Long feedId);

    /**
     * 检查用户是否订阅了指定RSS源
     */
    boolean existsByUserIdAndFeedId(Long userId, Long feedId);

    /**
     * 获取用户的所有订阅
     */
    List<UserRssSubscription> findByUserIdOrderByPriorityAscCreatedAtDesc(Long userId);

    /**
     * 获取用户收藏的RSS源
     */
    List<UserRssSubscription> findByUserIdAndIsFavoriteTrueOrderByPriorityAscCreatedAtDesc(Long userId);

    /**
     * 统计用户的订阅数量
     */
    long countByUserId(Long userId);

    /**
     * 删除用户对指定RSS源的订阅
     */
    void deleteByUserIdAndFeedId(Long userId, Long feedId);

    /**
     * 获取订阅了指定RSS源的所有用户ID
     */
    @Query("SELECT DISTINCT s.userId FROM UserRssSubscription s WHERE s.feedId = :feedId")
    List<Long> findUserIdsByFeedId(@Param("feedId") Long feedId);

    /**
     * 批量获取用户的订阅（带RSS源信息）
     */
    @Query("SELECT s FROM UserRssSubscription s LEFT JOIN FETCH s.id WHERE s.userId = :userId ORDER BY s.priority ASC, s.createdAt DESC")
    List<UserRssSubscription> findByUserIdWithDetails(@Param("userId") Long userId);
}
