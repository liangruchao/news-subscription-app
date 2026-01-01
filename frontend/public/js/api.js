// API 基础配置
const API_BASE_URL = 'http://localhost:8081/api';

// CSRF Token 缓存
let csrfToken = null;

/**
 * 获取 CSRF Token
 * 自动从后端获取并缓存
 */
async function getCsrfToken() {
    // 如果已有 token，直接返回
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
 * 刷新 CSRF Token
 * 在每次页面加载时调用
 */
async function refreshCsrfToken() {
    csrfToken = null;
    return await getCsrfToken();
}

// API 请求封装
async function apiRequest(url, options = {}) {
    try {
        // 对于需要修改数据的请求（POST, PUT, DELETE, PATCH），添加 CSRF Token
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
            credentials: 'include', // 携带 cookie
        });

        // 检查 HTTP 状态码
        if (!response.ok) {
            // 根据不同的 HTTP 状态码返回不同的错误信息
            let errorMessage = '请求失败';
            switch (response.status) {
                case 400:
                    errorMessage = '请求参数错误，请检查输入';
                    break;
                case 401:
                    errorMessage = '未登录，请先登录';
                    // 自动跳转到登录页
                    if (!window.location.pathname.endsWith('login.html') &&
                        !window.location.pathname.endsWith('register.html')) {
                        window.location.href = 'login.html';
                    }
                    break;
                case 403:
                    errorMessage = 'CSRF Token 无效或已过期，请刷新页面重试';
                    // CSRF Token 失效，清除缓存并刷新
                    csrfToken = null;
                    break;
                case 404:
                    errorMessage = '请求的资源不存在';
                    break;
                case 500:
                    errorMessage = '服务器内部错误，请稍后重试';
                    break;
                case 503:
                    errorMessage = '服务暂时不可用，请稍后重试';
                    break;
                default:
                    errorMessage = `请求失败 (${response.status})`;
            }

            // 尝试解析响应体中的错误信息
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

// 认证 API
const authAPI = {
    // 注册
    register: (username, email, password) => {
        return apiRequest('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, email, password }),
        });
    },

    // 登录
    login: (username, password) => {
        return apiRequest('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password }),
        });
    },

    // 登出
    logout: () => {
        return apiRequest('/auth/logout', {
            method: 'POST',
        });
    },

    // 获取当前用户
    getCurrentUser: () => {
        return apiRequest('/auth/current');
    },
};

// 订阅 API
const subscriptionAPI = {
    // 获取订阅列表
    getSubscriptions: () => {
        return apiRequest('/subscriptions');
    },

    // 添加订阅
    addSubscription: (category) => {
        return apiRequest('/subscriptions', {
            method: 'POST',
            body: JSON.stringify({ category }),
        });
    },

    // 取消订阅
    removeSubscription: (category) => {
        return apiRequest(`/subscriptions/${category}`, {
            method: 'DELETE',
        });
    },
};

// 新闻 API
const newsAPI = {
    // 获取用户新闻
    getUserNews: () => {
        return apiRequest('/news');
    },

    // 获取指定类别新闻
    getNewsByCategory: (category) => {
        return apiRequest(`/news/category/${category}`);
    },
};

