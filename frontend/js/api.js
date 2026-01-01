// API 基础配置
const API_BASE_URL = 'http://localhost:8081/api';

// API 请求封装
async function apiRequest(url, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            credentials: 'include', // 携带 cookie
        });

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
