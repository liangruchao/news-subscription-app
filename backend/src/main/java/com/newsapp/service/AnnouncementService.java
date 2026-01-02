package com.newsapp.service;

import com.newsapp.dto.CreateAnnouncementRequest;
import com.newsapp.entity.Announcement;
import com.newsapp.exception.BusinessException;
import com.newsapp.repository.AnnouncementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务
 */
@Service
public class AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementService.class);

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    /**
     * 创建公告
     */
    @Transactional
    public Announcement createAnnouncement(Long authorId, CreateAnnouncementRequest request) {
        log.info("创建公告: authorId={}, title={}", authorId, request.getTitle());

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setAuthorId(authorId);
        announcement.setIsPinned(request.getIsPinned() != null ? request.getIsPinned() : false);
        announcement.setIsPublished(false);

        Announcement saved = announcementRepository.save(announcement);
        log.info("公告创建成功: announcementId={}", saved.getId());
        return saved;
    }

    /**
     * 发布公告
     */
    @Transactional
    public Announcement publishAnnouncement(Long announcementId) {
        log.info("发布公告: announcementId={}", announcementId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (announcement.getIsPublished()) {
            throw new BusinessException("公告已发布");
        }

        announcement.setIsPublished(true);
        announcement.setPublishedAt(LocalDateTime.now());

        Announcement saved = announcementRepository.save(announcement);
        log.info("公告发布成功: announcementId={}", announcementId);
        return saved;
    }

    /**
     * 更新公告
     */
    @Transactional
    public Announcement updateAnnouncement(Long announcementId, CreateAnnouncementRequest request) {
        log.info("更新公告: announcementId={}", announcementId);

        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        if (request.getIsPinned() != null) {
            announcement.setIsPinned(request.getIsPinned());
        }

        Announcement saved = announcementRepository.save(announcement);
        log.info("公告更新成功: announcementId={}", announcementId);
        return saved;
    }

    /**
     * 删除公告
     */
    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        log.info("删除公告: announcementId={}", announcementId);

        if (!announcementRepository.existsById(announcementId)) {
            throw new BusinessException("公告不存在");
        }

        announcementRepository.deleteById(announcementId);
        log.info("公告删除成功: announcementId={}", announcementId);
    }

    /**
     * 获取已发布的公告列表
     */
    public List<Announcement> getPublishedAnnouncements() {
        return announcementRepository.findByIsPublishedTrueOrderByIsPinnedDescPublishedAtDescCreatedAtDesc();
    }

    /**
     * 获取所有公告（管理员）
     */
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 根据ID获取公告
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
    }
}
