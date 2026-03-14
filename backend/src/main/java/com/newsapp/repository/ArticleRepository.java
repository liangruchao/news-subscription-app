package com.newsapp.repository;

import com.newsapp.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文章 Repository
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * 根据feedId和guid查找文章（用于去重）
     */
    Optional<Article> findByFeedIdAndGuid(Long feedId, String guid);

    /**
     * 检查文章是否已存在
     */
    boolean existsByFeedIdAndGuid(Long feedId, String guid);

    /**
     * 根据RSS源ID查找文章（分页，按发布时间倒序）
     */
    Page<Article> findByFeedIdOrderByPubDateDesc(Long feedId, Pageable pageable);

    /**
     * 根据RSS源ID查找文章列表
     */
    List<Article> findByFeedIdOrderByPubDateDesc(Long feedId);

    /**
     * 根据分类查找文章（分页）
     */
    Page<Article> findByCategoryOrderByPubDateDesc(String category, Pageable pageable);

    /**
     * 查找最新文章（分页）
     */
    Page<Article> findAllByOrderByPubDateDesc(Pageable pageable);

    /**
     * 全文搜索（标题和内容）
     * 使用MySQL的FULLTEXT搜索
     */
    @Query(value = "SELECT * FROM articles WHERE " +
           "MATCH(title, content) AGAINST(:keyword IN NATURAL LANGUAGE MODE) " +
           "ORDER BY pub_date DESC",
           nativeQuery = true)
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 全文搜索（仅标题）
     */
    @Query(value = "SELECT * FROM articles WHERE " +
           "MATCH(title) AGAINST(:keyword IN NATURAL LANGUAGE MODE) " +
           "ORDER BY pub_date DESC",
           nativeQuery = true)
    Page<Article> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据多个RSS源ID查找文章
     */
    @Query("SELECT a FROM Article a WHERE a.feedId IN :feedIds ORDER BY a.pubDate DESC")
    Page<Article> findByFeedIdsOrderByPubDateDesc(@Param("feedIds") List<Long> feedIds, Pageable pageable);

    /**
     * 查找指定时间段内的文章
     */
    @Query("SELECT a FROM Article a WHERE a.pubDate BETWEEN :startDate AND :endDate ORDER BY a.pubDate DESC")
    List<Article> findByPubDateBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 统计RSS源的文章数量
     */
    long countByFeedId(Long feedId);

    /**
     * 删除指定RSS源的所有文章
     */
    @Modifying
    @Transactional
    void deleteByFeedId(Long feedId);

    /**
     * 增加文章浏览次数
     */
    @Modifying
    @Transactional
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :articleId")
    void incrementViewCount(@Param("articleId") Long articleId);

    /**
     * 查找热门文章（按浏览次数排序）
     */
    @Query("SELECT a FROM Article a WHERE a.pubDate > :since ORDER BY a.viewCount DESC")
    Page<Article> findPopularArticles(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * 查找最新文章（限制数量）
     */
    @Query("SELECT a FROM Article a ORDER BY a.pubDate DESC")
    List<Article> findLatestArticles(Pageable pageable);

    /**
     * 统计指定时间段的文章数量
     */
    long countByPubDateAfter(LocalDateTime date);
}
