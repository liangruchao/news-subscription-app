package com.newsapp.repository;

import com.newsapp.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 登录历史 Repository
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 获取用户的登录历史，按时间倒序
     */
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    /**
     * 获取用户最近N次登录记录
     */
    List<LoginHistory> findTop10ByUserIdOrderByLoginTimeDesc(Long userId);
}
