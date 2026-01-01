/**
 * 国际化 (i18n) 模块
 * 支持中英文切换
 */

// 语言包
const i18n = {
    'zh-CN': {
        // 通用
        common: {
            save: '保存',
            cancel: '取消',
            delete: '删除',
            edit: '编辑',
            confirm: '确认',
            loading: '加载中...',
            success: '成功',
            error: '错误',
            warning: '警告',
            logout: '退出登录',
            back: '返回',
            submit: '提交',
            reset: '重置',
            search: '搜索',
            refresh: '刷新',
        },

        // 导航栏
        nav: {
            home: '首页',
            profile: '个人中心',
            messages: '消息中心',
            announcements: '系统公告',
            preferences: '偏好设置',
            logout: '退出登录',
        },

        // 个人中心
        profile: {
            title: '个人中心',
            basicInfo: '基本信息',
            username: '用户名',
            email: '邮箱',
            bio: '个人简介',
            avatar: '头像',
            changeAvatar: '更换头像',
            uploadAvatar: '上传头像',
            saveProfile: '保存资料',
            changePassword: '修改密码',
            oldPassword: '原密码',
            newPassword: '新密码',
            confirmPassword: '确认密码',
            passwordNotMatch: '两次输入的新密码不一致',
            passwordTooShort: '新密码长度至少为6位',
            changePasswordSuccess: '密码修改成功！请重新登录',
            deleteAccount: '注销账户',
            deleteAccountWarning: '警告：此操作将永久删除您的账户和所有数据，且无法恢复！',
            deleteAccountConfirm: '您确定要注销账户吗？',
            deleteAccountDoubleConfirm: '为了确认，请输入 "DELETE" 来继续注销账户：',
            deleteAccountCancelled: '已取消注销账户操作',
            deleteAccountSuccess: '账户已成功注销',
            profileUpdateSuccess: '个人资料更新成功！',
            usernameEmailRequired: '用户名和邮箱不能为空',
            dangerZone: '危险操作',
        },

        // 消息中心
        messages: {
            title: '消息中心',
            all: '全部',
            unread: '未读',
            markAllRead: '全部标为已读',
            clearHistory: '清空历史',
            noMessages: '暂无消息',
            systemMessage: '系统消息',
            subscriptionMessage: '订阅消息',
            newsMessage: '新闻消息',
        },

        // 系统公告
        announcements: {
            title: '系统公告',
            published: '已发布',
            draft: '草稿',
            pinned: '置顶',
            noAnnouncements: '暂无公告',
        },

        // 偏好设置
        preferences: {
            title: '偏好设置',
            notificationSettings: '通知设置',
            newsNotification: '新闻通知',
            newsNotificationDesc: '当有新的新闻内容时接收通知',
            systemNotification: '系统通知',
            systemNotificationDesc: '接收系统消息和更新提醒',
            subscriptionNotification: '订阅通知',
            subscriptionNotificationDesc: '订阅状态发生变化时接收通知',
            displaySettings: '显示设置',
            newsPageSize: '新闻每页数量',
            newsPageSizeDesc: '设置每次加载新闻时的显示数量',
            compactMode: '紧凑模式',
            compactModeDesc: '使用更紧凑的界面布局',
            languageSettings: '语言设置',
            interfaceLanguage: '界面语言',
            interfaceLanguageDesc: '选择系统界面显示语言',
            privacySettings: '隐私设置',
            publicProfile: '公开资料',
            publicProfileDesc: '允许其他用户查看您的基本信息',
            showOnlineStatus: '显示在线状态',
            showOnlineStatusDesc: '显示您的最后登录时间',
            saving: '保存中...',
            saved: '设置已保存',
            saveFailed: '保存失败',
            resetToDefault: '恢复默认设置',
        },

        // 首页
        home: {
            welcome: '欢迎',
            mySubscriptions: '我的订阅',
            addSubscription: '添加订阅',
            manageSubscriptions: '管理订阅',
            noSubscriptions: '您还没有订阅任何类别',
            goToSubscribe: '去订阅',
            latestNews: '最新新闻',
            loadMore: '加载更多',
            noNews: '暂无新闻',
        },

        // 登录/注册
        auth: {
            login: '登录',
            register: '注册',
            username: '用户名',
            password: '密码',
            email: '邮箱',
            confirmPassword: '确认密码',
            forgotPassword: '忘记密码？',
            noAccount: '还没有账户？',
            hasAccount: '已有账户？',
            goToRegister: '去注册',
            goToLogin: '去登录',
            loginSuccess: '登录成功！正在跳转...',
            registerSuccess: '注册成功！正在跳转...',
            loginFailed: '登录失败',
            registerFailed: '注册失败',
            passwordNotMatch: '两次输入的密码不一致',
            passwordTooShort: '密码长度至少为6位',
        },

        // 统计信息
        stats: {
            title: '我的统计',
            subscriptionCount: '订阅数量',
            memberDays: '加入天数',
            loginHistory: '登录历史',
            currentLogin: '当前',
            loginTime: '登录时间',
            ipAddress: 'IP地址',
            noHistory: '暂无登录记录',
            minutesAgo: '分钟前',
            hoursAgo: '小时前',
            daysAgo: '天前',
            justNow: '刚刚',
        },
    },

    'en-US': {
        // Common
        common: {
            save: 'Save',
            cancel: 'Cancel',
            delete: 'Delete',
            edit: 'Edit',
            confirm: 'Confirm',
            loading: 'Loading...',
            success: 'Success',
            error: 'Error',
            warning: 'Warning',
            logout: 'Logout',
            back: 'Back',
            submit: 'Submit',
            reset: 'Reset',
            search: 'Search',
            refresh: 'Refresh',
        },

        // Navigation
        nav: {
            home: 'Home',
            profile: 'Profile',
            messages: 'Messages',
            announcements: 'Announcements',
            preferences: 'Preferences',
            logout: 'Logout',
        },

        // Profile
        profile: {
            title: 'Profile',
            basicInfo: 'Basic Information',
            username: 'Username',
            email: 'Email',
            bio: 'Bio',
            avatar: 'Avatar',
            changeAvatar: 'Change Avatar',
            uploadAvatar: 'Upload Avatar',
            saveProfile: 'Save Profile',
            changePassword: 'Change Password',
            oldPassword: 'Old Password',
            newPassword: 'New Password',
            confirmPassword: 'Confirm Password',
            passwordNotMatch: 'Passwords do not match',
            passwordTooShort: 'Password must be at least 6 characters',
            changePasswordSuccess: 'Password changed successfully! Please login again',
            deleteAccount: 'Delete Account',
            deleteAccountWarning: 'Warning: This will permanently delete your account and all data, and cannot be undone!',
            deleteAccountConfirm: 'Are you sure you want to delete your account?',
            deleteAccountDoubleConfirm: 'To confirm, please type "DELETE" to continue:',
            deleteAccountCancelled: 'Account deletion cancelled',
            deleteAccountSuccess: 'Account successfully deleted',
            profileUpdateSuccess: 'Profile updated successfully!',
            usernameEmailRequired: 'Username and email are required',
            dangerZone: 'Danger Zone',
        },

        // Messages
        messages: {
            title: 'Message Center',
            all: 'All',
            unread: 'Unread',
            markAllRead: 'Mark All as Read',
            clearHistory: 'Clear History',
            noMessages: 'No messages',
            systemMessage: 'System Message',
            subscriptionMessage: 'Subscription Message',
            newsMessage: 'News Message',
        },

        // Announcements
        announcements: {
            title: 'Announcements',
            published: 'Published',
            draft: 'Draft',
            pinned: 'Pinned',
            noAnnouncements: 'No announcements',
        },

        // Preferences
        preferences: {
            title: 'Preferences',
            notificationSettings: 'Notification Settings',
            newsNotification: 'News Notifications',
            newsNotificationDesc: 'Receive notifications when new content is available',
            systemNotification: 'System Notifications',
            systemNotificationDesc: 'Receive system messages and update reminders',
            subscriptionNotification: 'Subscription Notifications',
            subscriptionNotificationDesc: 'Get notified when subscription status changes',
            displaySettings: 'Display Settings',
            newsPageSize: 'News Page Size',
            newsPageSizeDesc: 'Set the number of news items to display per page',
            compactMode: 'Compact Mode',
            compactModeDesc: 'Enable compact mode to display more content',
            languageSettings: 'Language Settings',
            interfaceLanguage: 'Interface Language',
            interfaceLanguageDesc: 'Select the system interface display language',
            privacySettings: 'Privacy Settings',
            publicProfile: 'Public Profile',
            publicProfileDesc: 'Allow other users to view your basic information',
            showOnlineStatus: 'Show Online Status',
            showOnlineStatusDesc: 'Display your last login time',
            saving: 'Saving...',
            saved: 'Settings saved',
            saveFailed: 'Save failed',
            resetToDefault: 'Reset to Default',
        },

        // Home
        home: {
            welcome: 'Welcome',
            mySubscriptions: 'My Subscriptions',
            addSubscription: 'Add Subscription',
            manageSubscriptions: 'Manage Subscriptions',
            noSubscriptions: 'You have no subscriptions yet',
            goToSubscribe: 'Go to Subscribe',
            latestNews: 'Latest News',
            loadMore: 'Load More',
            noNews: 'No news available',
        },

        // Auth
        auth: {
            login: 'Login',
            register: 'Register',
            username: 'Username',
            password: 'Password',
            email: 'Email',
            confirmPassword: 'Confirm Password',
            forgotPassword: 'Forgot Password?',
            noAccount: "Don't have an account?",
            hasAccount: 'Already have an account?',
            goToRegister: 'Go to Register',
            goToLogin: 'Go to Login',
            loginSuccess: 'Login successful! Redirecting...',
            registerSuccess: 'Registration successful! Redirecting...',
            loginFailed: 'Login failed',
            registerFailed: 'Registration failed',
            passwordNotMatch: 'Passwords do not match',
            passwordTooShort: 'Password must be at least 6 characters',
        },

        // Stats
        stats: {
            title: 'My Statistics',
            subscriptionCount: 'Subscriptions',
            memberDays: 'Member Days',
            loginHistory: 'Login History',
            currentLogin: 'Current',
            loginTime: 'Login Time',
            ipAddress: 'IP Address',
            noHistory: 'No login history',
            minutesAgo: 'minutes ago',
            hoursAgo: 'hours ago',
            daysAgo: 'days ago',
            justNow: 'just now',
        },
    },
};

// 当前语言
let currentLanguage = 'zh-CN';

console.log('[i18n] i18n.js loaded, current language:', currentLanguage);

/**
 * 获取翻译文本
 * @param {string} key - 翻译键，使用点号分隔，如 'profile.title'
 * @param {string} lang - 语言代码，默认使用当前语言
 * @returns {string} 翻译后的文本
 */
function t(key, lang = currentLanguage) {
    const keys = key.split('.');
    let value = i18n[lang];

    console.log(`[i18n] t() called with key="${key}", `lang="${lang}"`, `initial value:`, value);

    for (const k of keys) {
        console.log(`[i18n] Looking for key "${k}" in value:`, value);
        if (value && value[k] !== undefined) {
            value = value[k];
        } else {
            // 如果翻译不存在，返回 key 本身
            console.warn(`[i18n] Translation not found: ${key} for language: ${lang}`);
            console.warn(`[i18n] Available keys in ${lang}:`, Object.keys(i18n[lang] || {}));
            return key;
        }
    }

    console.log(`[i18n] Final translation: ${key} =`, value);
    return value;
}

/**
 * 设置当前语言
 * @param {string} lang - 语言代码 ('zh-CN' 或 'en-US')
 */
function setLanguage(lang) {
    if (i18n[lang]) {
        console.log(`[i18n] Setting language to: ${lang}`);
        currentLanguage = lang;
        // 保存到 localStorage
        localStorage.setItem('preferredLanguage', lang);
        // 更新页面中所有带有 data-i18n 属性的元素
        updatePageLanguage();
    } else {
        console.error(`Language not supported: ${lang}`);
    }
}

/**
 * 获取当前语言
 * @returns {string} 当前语言代码
 */
function getLanguage() {
    return currentLanguage;
}

/**
 * 更新页面中所有带 data-i18n 属性的元素
 */
function updatePageLanguage() {
    // 更新所有带有 data-i18n 属性的元素
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        const translation = t(key);

        // 检查元素是否有子元素（除了纯文本节点）
        const hasChildElements = Array.from(element.childNodes).some(node => node.nodeType === Node.ELEMENT_NODE);

        if (hasChildElements) {
            // 如果有子元素，只更新第一个文本节点
            let firstTextNode = null;
            for (const child of element.childNodes) {
                if (child.nodeType === Node.TEXT_NODE) {
                    firstTextNode = child;
                    break;
                }
            }
            if (firstTextNode) {
                firstTextNode.textContent = translation;
            } else {
                // 如果没有文本节点，在开头插入一个
                element.insertBefore(document.createTextNode(translation), element.firstChild);
            }
        } else {
            // 如果没有子元素，直接替换文本
            element.textContent = translation;
        }
    });

    // 更新所有带有 data-i18n-placeholder 属性的元素
    document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
        const key = element.getAttribute('data-i18n-placeholder');
        const translation = t(key);
        element.placeholder = translation;
    });

    // 更新所有带有 data-i18n-title 属性的元素
    document.querySelectorAll('[data-i18n-title]').forEach(element => {
        const key = element.getAttribute('data-i18n-title');
        const translation = t(key);
        element.title = translation;
    });

    // 触发自定义事件，让页面可以监听语言变化
    document.dispatchEvent(new CustomEvent('languageChanged', { detail: { language: currentLanguage } }));
}

/**
 * 初始化语言设置
 * 从 localStorage 或用户偏好中读取语言设置
 */
async function initializeLanguage() {
    // 1. 先尝试从 localStorage 读取
    let lang = localStorage.getItem('preferredLanguage');

    // 2. 如果没有，尝试从用户偏好获取
    if (!lang) {
        try {
            const result = await preferencesAPI.getPreferences();
            if (result.success && result.data && result.data.language) {
                lang = result.data.language;
            }
        } catch (error) {
            console.warn('Failed to get language preference:', error);
        }
    }

    // 3. 如果还是没有，使用浏览器语言
    if (!lang) {
        const browserLang = navigator.language || navigator.userLanguage;
        if (browserLang.startsWith('zh')) {
            lang = 'zh-CN';
        } else if (browserLang.startsWith('en')) {
            lang = 'en-US';
        } else {
            lang = 'zh-CN'; // 默认中文
        }
    }

    // 设置语言
    setLanguage(lang);
}

// 在 DOM 加载完成后初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeLanguage);
} else {
    initializeLanguage();
}
