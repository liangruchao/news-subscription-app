package com.newsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * News Subscription Application
 * 主应用程序入口
 */
@SpringBootApplication
@EnableCaching
public class NewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("News Subscription Application Started!");
        System.out.println("Access the application at: http://localhost:8081");
        System.out.println("========================================\n");
    }
}
