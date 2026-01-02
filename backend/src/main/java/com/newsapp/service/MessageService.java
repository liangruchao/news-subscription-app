package com.newsapp.service;

import com.newsapp.dto.SendMessageRequest;
import com.newsapp.entity.Message;
import com.newsapp.exception.BusinessException;
import com.newsapp.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务
 */
@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * 发送消息给指定用户
     */
    @Transactional
    public Message sendMessage(Long userId, SendMessageRequest request) {
        log.info("发送消息: userId={}, title={}", userId, request.getTitle());

        Message message = new Message();
        message.setUserId(userId);
        message.setSenderId(request.getSenderId());
        message.setTitle(request.getTitle());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : "SYSTEM");

        Message saved = messageRepository.save(message);
        log.info("消息发送成功: messageId={}", saved.getId());
        return saved;
    }

    /**
     * 获取用户的所有消息
     */
    public List<Message> getUserMessages(Long userId) {
        return messageRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取用户的未读消息
     */
    public List<Message> getUnreadMessages(Long userId) {
        return messageRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取未读消息数量
     */
    public long getUnreadCount(Long userId) {
        return messageRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 标记消息为已读
     */
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));

        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此消息");
        }

        if (!message.getIsRead()) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
            log.info("消息标记为已读: messageId={}", messageId);
        }
    }

    /**
     * 标记所有消息为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Message> unreadMessages = getUnreadMessages(userId);
        LocalDateTime now = LocalDateTime.now();

        for (Message message : unreadMessages) {
            message.setIsRead(true);
            message.setReadAt(now);
        }

        messageRepository.saveAll(unreadMessages);
        log.info("标记所有消息为已读: userId={}, count={}", userId, unreadMessages.size());
    }

    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("消息不存在"));

        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此消息");
        }

        messageRepository.delete(message);
        log.info("删除消息: messageId={}", messageId);
    }
}
