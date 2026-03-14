package com.newsapp.repository;

import com.newsapp.entity.ArticleShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文章分享Repository
 */
@Repository
public interface ArticleShareRepository extends JpaRepository<ArticleShare, Long> {

    /**
     * 根据分享码查找
     */
    Optional<ArticleShare> findByShareCode(String shareCode);

    /**
     * 查找用户的分享列表
     */
    List<ArticleShare> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 查找有效的分享
     */
    @Query("SELECT s FROM ArticleShare s WHERE s.shareCode = :shareCode AND s.isActive = true AND (s.expiresAt IS NULL OR s.expiresAt > :now)")
    Optional<ArticleShare> findActiveShare(@Param("shareCode") String shareCode, @Param("now") LocalDateTime now);

    /**
     * 查找过期的分享
     */
    @Query("SELECT s FROM ArticleShare s WHERE s.isActive = true AND s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    List<ArticleShare> findExpiredShares(@Param("now") LocalDateTime now);

    /**
     * 统计用户的分享数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户今日分享数量
     */
    @Query("SELECT COUNT(s) FROM ArticleShare s WHERE s.userId = :userId AND DATE(s.createdAt) = CURRENT_DATE")
    long countByUserIdToday(@Param("userId") Long userId);

    /**
     * 增加浏览次数
     */
    @Modifying
    @Query("UPDATE ArticleShare s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 删除过期的分享
     */
    @Modifying
    @Query("DELETE FROM ArticleShare s WHERE s.isActive = false AND s.expiresAt < :date")
    int deleteOldExpiredShares(@Param("date") LocalDateTime date);
}
