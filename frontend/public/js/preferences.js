// 当前偏好设置
let currentPreferences = null;
let saveTimeout = null;

console.log('[Preferences] preferences.js loaded');

/**
 * 加载用户偏好设置
 */
async function loadPreferences() {
    console.log('[Preferences] loadPreferences called');
    const result = await preferencesAPI.getPreferences();
    if (result.success) {
        currentPreferences = result.data || {};
        displayPreferences(currentPreferences);

        // 同步语言设置到 i18n
        if (currentPreferences.language) {
            setLanguage(currentPreferences.language);
        }
    } else {
        console.error('加载偏好设置失败:', result.message);
    }
}

/**
 * 显示偏好设置
 */
function displayPreferences(preferences) {
    // 通知设置
    document.getElementById('newsNotificationEnabled').checked = preferences.newsNotificationEnabled !== false;
    document.getElementById('systemNotificationEnabled').checked = preferences.systemNotificationEnabled !== false;
    document.getElementById('subscriptionNotificationEnabled').checked = preferences.subscriptionNotificationEnabled !== false;

    // 显示设置
    document.getElementById('newsPageSize').value = preferences.newsPageSize || 20;
    document.getElementById('compactMode').checked = preferences.compactMode || false;

    // 语言设置
    document.getElementById('language').value = preferences.language || 'zh-CN';

    // 隐私设置
    document.getElementById('publicProfile').checked = preferences.publicProfile !== false;
    document.getElementById('showOnlineStatus').checked = preferences.showOnlineStatus !== false;
}

/**
 * 处理语言切换
 */
async function handleLanguageChange() {
    console.log('[Preferences] handleLanguageChange called!');
    const newLanguage = document.getElementById('language').value;
    console.log(`[Preferences] Language changed to: ${newLanguage}`);

    // 检查 setLanguage 函数是否存在
    if (typeof setLanguage !== 'function') {
        console.error('[Preferences] setLanguage is not a function!');
        return;
    }

    // 1. 立即更新界面语言
    setLanguage(newLanguage);

    // 2. 保存到后端
    await savePreferences();
}

/**
 * 保存偏好设置
 */
async function savePreferences() {
    // 清除之前的定时器
    if (saveTimeout) {
        clearTimeout(saveTimeout);
    }

    // 显示保存中提示
    showSaveMessage(t('preferences.saving'), false);

    // 延迟保存（避免频繁请求）
    saveTimeout = setTimeout(async () => {
        const preferences = {
            // 通知设置
            newsNotificationEnabled: document.getElementById('newsNotificationEnabled').checked,
            systemNotificationEnabled: document.getElementById('systemNotificationEnabled').checked,
            subscriptionNotificationEnabled: document.getElementById('subscriptionNotificationEnabled').checked,

            // 显示设置
            newsPageSize: parseInt(document.getElementById('newsPageSize').value),
            compactMode: document.getElementById('compactMode').checked,

            // 语言设置
            language: document.getElementById('language').value,

            // 隐私设置
            publicProfile: document.getElementById('publicProfile').checked,
            showOnlineStatus: document.getElementById('showOnlineStatus').checked,
        };

        const result = await preferencesAPI.updatePreferences(preferences);
        if (result.success) {
            currentPreferences = result.data || preferences;

            // 如果语言设置改变了，更新 localStorage
            if (preferences.language) {
                localStorage.setItem('preferredLanguage', preferences.language);
            }

            showSaveMessage(t('preferences.saved'), true);
        } else {
            showSaveMessage(t('preferences.saveFailed') + ': ' + result.message, false);
        }
    }, 500); // 延迟 500ms 保存
}

/**
 * 显示保存消息
 */
function showSaveMessage(message, success) {
    const saveMessage = document.getElementById('saveMessage');
    const saveText = saveMessage.querySelector('.save-text');
    const saveIcon = saveMessage.querySelector('.save-icon');

    saveText.textContent = message;
    saveIcon.textContent = success ? '✓' : '✗';
    saveMessage.style.background = success ? '#4caf50' : '#f44336';

    saveMessage.classList.add('show');

    // 3秒后隐藏
    setTimeout(() => {
        saveMessage.classList.remove('show');
    }, 3000);
}

/**
 * 更新消息徽章
 */
async function updateMessageBadge() {
    const result = await messageAPI.getUnreadCount();
    if (result.success && result.data > 0) {
        const badge = document.getElementById('messageBadge');
        if (badge) {
            badge.textContent = result.data > 99 ? '99+' : result.data;
        }
    } else {
        const badge = document.getElementById('messageBadge');
        if (badge) {
            badge.textContent = '';
        }
    }
}

/**
 * 获取当前偏好设置（供其他页面使用）
 */
function getPreferences() {
    return currentPreferences || {};
}

/**
 * 获取新闻每页数量
 */
function getNewsPageSize() {
    return currentPreferences?.newsPageSize || 20;
}

/**
 * 检查是否启用紧凑模式
 */
function isCompactMode() {
    return currentPreferences?.compactMode || false;
}

/**
 * 检查是否启用新闻通知
 */
function isNewsNotificationEnabled() {
    return currentPreferences?.newsNotificationEnabled !== false;
}
