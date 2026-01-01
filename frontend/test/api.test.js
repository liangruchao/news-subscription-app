/**
 * API 模块单元测试
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import { resetMocks } from './setup.js';

// API 基础配置
const API_BASE_URL = 'http://localhost:8081/api';

// CSRF Token 缓存
let csrfToken = null;

/**
 * 获取 CSRF Token
 */
async function getCsrfToken() {
  if (csrfToken) {
    return csrfToken;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/csrf`, {
      credentials: 'include',
    });

    if (response.ok) {
      const data = await response.json();
      csrfToken = data.token;
      return csrfToken;
    }
  } catch (error) {
    console.warn('获取 CSRF Token 失败:', error);
  }

  return null;
}

/**
 * API 请求封装
 */
async function apiRequest(url, options = {}) {
  try {
    const method = (options.method || 'GET').toUpperCase();
    if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
      const token = await getCsrfToken();
      if (token) {
        options.headers = {
          ...options.headers,
          'X-CSRF-TOKEN': token,
        };
      }
    }

    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      credentials: 'include',
    });

    if (!response.ok) {
      let errorMessage = '请求失败';
      switch (response.status) {
        case 400:
          errorMessage = '请求参数错误，请检查输入';
          break;
        case 401:
          errorMessage = '未登录，请先登录';
          break;
        case 403:
          errorMessage = 'CSRF Token 无效或已过期，请刷新页面重试';
          csrfToken = null;
          break;
        case 404:
          errorMessage = '请求的资源不存在';
          break;
        case 500:
          errorMessage = '服务器内部错误，请稍后重试';
          break;
        default:
          errorMessage = `请求失败 (${response.status})`;
      }

      const errorData = await response.json().catch(() => null);
      if (errorData && errorData.message) {
        errorMessage = errorData.message;
      }

      return {
        success: false,
        message: errorMessage
      };
    }

    const data = await response.json();
    return data;
  } catch (error) {
    console.error('API 请求失败:', error);
    return {
      success: false,
      message: '网络错误，请稍后重试'
    };
  }
}

describe('apiRequest', () => {
  beforeEach(() => {
    resetMocks();
  });

  describe('CSRF Token 处理', () => {
    it('POST 请求应该自动添加 CSRF Token', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ token: 'test-token-abc' })
      });

      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: {} })
      });

      await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username: 'test', password: 'test' })
      });

      // 第二次调用应该包含 CSRF token
      const secondCall = global.fetch.mock.calls[1];
      expect(secondCall[1].headers['X-CSRF-TOKEN']).toBe('test-token-abc');
    });

    it('GET 请求不应该添加 CSRF Token', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => ({ success: true, data: {} })
      });

      await apiRequest('/auth/current');

      const call = global.fetch.mock.calls[0];
      expect(call[1].headers['X-CSRF-TOKEN']).toBeUndefined();
    });
  });

  describe('HTTP 状态码处理', () => {
    it('401 状态码应该返回未登录错误', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ message: 'Unauthorized' })
      });

      const result = await apiRequest('/test');

      expect(result.success).toBe(false);
      expect(result.message).toBe('未登录，请先登录');
    });

    it('403 状态码应该清除 CSRF Token', async () => {
      csrfToken = 'existing-token';
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 403,
        json: async () => ({ message: 'Forbidden' })
      });

      await apiRequest('/test', { method: 'POST' });

      expect(csrfToken).toBeNull();
    });

    it('404 状态码应该返回资源不存在错误', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        json: async () => ({ message: 'Not Found' })
      });

      const result = await apiRequest('/test');

      expect(result.success).toBe(false);
      expect(result.message).toBe('请求的资源不存在');
    });

    it('500 状态码应该返回服务器错误', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ message: 'Internal Server Error' })
      });

      const result = await apiRequest('/test');

      expect(result.success).toBe(false);
      expect(result.message).toBe('服务器内部错误，请稍后重试');
    });
  });

  describe('错误处理', () => {
    it('网络错误应该返回网络错误提示', async () => {
      global.fetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await apiRequest('/test');

      expect(result.success).toBe(false);
      expect(result.message).toBe('网络错误，请稍后重试');
    });

    it('应该使用响应体中的错误信息', async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: async () => ({ message: '用户名不能为空' })
      });

      const result = await apiRequest('/test');

      expect(result.success).toBe(false);
      expect(result.message).toBe('用户名不能为空');
    });
  });

  describe('成功响应', () => {
    it('成功响应应该返回解析后的 JSON 数据', async () => {
      const mockData = { success: true, data: { id: 1, name: 'Test' } };
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockData
      });

      const result = await apiRequest('/test');

      expect(result).toEqual(mockData);
    });
  });
});
