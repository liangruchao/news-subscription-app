package com.newsapp.repository;

import com.newsapp.entity.UserFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户收藏 Repository
 */
@Repository
public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    /**
     * 查找用户对指定文章的收藏
     */
    Optional<UserFavorite> findByUserIdAndArticleId(Long userId, Long articleId);

    /**
     * 检查用户是否收藏了指定文章
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    /**
     * 获取用户的所有收藏（按创建时间倒序）
     */
    Page<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 获取用户的所有收藏
     */
    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根据标签筛选用户的收藏
     */
    @Query("SELECT f FROM UserFavorite f WHERE f.userId = :userId AND f.tags LIKE %:tag%")
    Page<UserFavorite> findByUserIdAndTag(@Param("userId") Long userId, @Param("tag") String tag, Pageable pageable);

    /**
     * 获取用户未读的收藏
     */
    Page<UserFavorite> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 获取用户已读的收藏
     */
    Page<UserFavorite> findByUserIdAndIsReadTrueOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 统计用户的收藏数量
     */
    long countByUserId(Long userId);

    /**
     * 统计用户未读收藏数量
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 获取用户的所有标签（去重）
     * 注意：标签存储在tags字段中，用逗号分隔
     * 这个方法返回所有包含标签的收藏记录
     */
    @Query("SELECT DISTINCT f.tags FROM UserFavorite f WHERE f.userId = :userId AND f.tags IS NOT NULL AND f.tags != ''")
    List<String> findDistinctTagsByUserId(@Param("userId") Long userId);

    /**
     * 删除用户对指定文章的收藏
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
}
