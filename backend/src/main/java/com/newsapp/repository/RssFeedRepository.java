package com.newsapp.repository;

import com.newsapp.entity.RssFeed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * RSS源 Repository
 */
@Repository
public interface RssFeedRepository extends JpaRepository<RssFeed, Long> {

    /**
     * 根据URL查找RSS源
     */
    Optional<RssFeed> findByUrl(String url);

    /**
     * 检查URL是否存在
     */
    boolean existsByUrl(String url);

    /**
     * 根据分类查找所有激活的RSS源
     */
    List<RssFeed> findByCategoryAndIsActive(String category, boolean isActive);

    /**
     * 查找所有激活的RSS源
     */
    @Query(value = "SELECT * FROM rss_feeds WHERE is_active = 1", nativeQuery = true)
    List<RssFeed> findActiveFeeds();

    /**
     * 根据分类查找RSS源
     */
    List<RssFeed> findByCategory(String category);

    /**
     * 查找需要抓取的RSS源（激活状态且超过抓取间隔）
     */
    @Query("SELECT rf FROM RssFeed rf WHERE rf.isActive = true AND " +
           "(rf.lastFetchedAt IS NULL OR rf.lastFetchedAt < :threshold)")
    List<RssFeed> findFeedsToFetch(@Param("threshold") LocalDateTime threshold);

    /**
     * 根据标题模糊搜索
     */
    @Query("SELECT rf FROM RssFeed rf WHERE rf.title LIKE %:keyword%")
    List<RssFeed> searchByTitle(@Param("keyword") String keyword);

    /**
     * 查找所有分类（去重）
     */
    @Query("SELECT DISTINCT rf.category FROM RssFeed rf WHERE rf.category IS NOT NULL ORDER BY rf.category")
    List<String> findAllCategories();
}
