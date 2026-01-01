package com.newsapp.repository;

import com.newsapp.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 公告 Repository
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * 获取已发布的公告，按置顶和发布时间倒序
     */
    List<Announcement> findByIsPublishedTrueOrderByIsPinnedDescPublishedAtDescCreatedAtDesc();

    /**
     * 获取所有公告（包括未发布），按创建时间倒序
     */
    List<Announcement> findAllByOrderByCreatedAtDesc();
}
