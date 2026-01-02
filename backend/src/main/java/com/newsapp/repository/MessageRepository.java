package com.newsapp.repository;

import com.newsapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息 Repository
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 获取用户的所有消息，按创建时间倒序
     */
    List<Message> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 获取用户的未读消息
     */
    List<Message> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 统计用户未读消息数量
     */
    long countByUserIdAndIsReadFalse(Long userId);
}
