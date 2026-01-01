package com.newsapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 密码修复工具
 * 用于将数据库中的明文密码转换为 BCrypt 加密密码
 *
 * 使用方法：
 * 1. 修改数据库连接信息
 * 2. 运行 main 方法
 */
public class PasswordFixer {

    public static void main(String[] args) {
        // 数据库连接信息
        String url = "jdbc:mysql://localhost:3306/news_app";
        String username = "root";
        String password = "apeng320";

        PasswordEncoder encoder = new BCryptPasswordEncoder(10);

        try (Connection conn = DriverManager.getConnection(url, username, password)) {

            // 查询所有用户
            String selectSql = "SELECT id, username, password FROM users";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();

            int fixedCount = 0;

            while (rs.next()) {
                Long id = rs.getLong("id");
                String user = rs.getString("username");
                String pwd = rs.getString("password");

                // 如果密码不是 BCrypt 格式（不以 $2a$ 开头），则需要加密
                if (pwd == null || pwd.isEmpty() || !pwd.startsWith("$2a$")) {
                    String newHash = encoder.encode(pwd);
                    System.out.println("修复用户: " + user + " | 旧密码: " + pwd + " | 新哈希: " + newHash);

                    // 更新数据库
                    String updateSql = "UPDATE users SET password = ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setString(1, newHash);
                    updateStmt.setLong(2, id);
                    updateStmt.executeUpdate();
                    updateStmt.close();

                    fixedCount++;
                } else {
                    System.out.println("跳过用户: " + user + " (密码已加密)");
                }
            }

            System.out.println("\n修复完成！共修复 " + fixedCount + " 个用户的密码。");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
