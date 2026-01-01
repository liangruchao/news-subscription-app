package com.newsapp.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 哈希生成器
 */
public class HashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // 生成需要修复的密码哈希
        String[] passwords = {
            "apeng320",      // ruchao
            "apeng320!",     // ruchaol
            "Password123",   // testuser 等
            "12345",         // user1767271990
            "123",           // user1767272055
            "password123"    // usernocap1767272109
        };

        System.out.println("-- BCrypt 哈希生成结果 --");
        System.out.println("-- 复制下面的 SQL 语句到 MySQL 执行:\n");

        for (String pwd : passwords) {
            String hash = encoder.encode(pwd);
            System.out.println("-- 密码: " + pwd);
            System.out.println("UPDATE users SET password = '" + hash + "' WHERE password = '" + pwd + "';");
            System.out.println();
        }
    }
}
