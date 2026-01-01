package com.newsapp.exception;

/**
 * 资源不存在异常
 * 用于处理请求的资源不存在的情况
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s 不存在，ID: %d", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s 不存在，%s: %s", resourceName, fieldName, fieldValue));
    }
}
