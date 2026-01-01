package com.newsapp.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.context.annotation.Bean;

/**
 * 验证配置
 * 启用方法级别的验证以支持 @Valid 和 @Validated 注解
 */
@AutoConfiguration
public class ValidationConfig {

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
