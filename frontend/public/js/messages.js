// 消息列表数据
let allMessages = [];
let currentFilter = 'all';
let currentMessageId = null;

/**
 * 加载消息列表
 */
async function loadMessages() {
    const result = await messageAPI.getAllMessages();
    if (result.success) {
        allMessages = result.data || [];
        filterMessages(currentFilter);
    } else {
        showMessage('加载消息失败: ' + result.message, 'error');
    }
}

/**
 * 过滤消息
 */
function filterMessages(filter) {
    currentFilter = filter;

    // 更新过滤按钮状态
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event?.target?.classList?.add('active');

    // 过滤消息
    let filteredMessages = allMessages;
    if (filter === 'unread') {
        filteredMessages = allMessages.filter(msg => !msg.read);
    } else if (filter === 'read') {
        filteredMessages = allMessages.filter(msg => msg.read);
    }

    displayMessages(filteredMessages);
}

/**
 * 显示消息列表
 */
function displayMessages(messages) {
    const messagesList = document.getElementById('messagesList');
    const emptyState = document.getElementById('emptyState');

    if (messages.length === 0) {
        messagesList.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';
    messagesList.innerHTML = messages.map(msg => createMessageItem(msg)).join('');
}

/**
 * 创建消息项
 */
function createMessageItem(message) {
    const icon = getMessageIcon(message.type);
    const time = formatTime(message.createdAt);
    const unreadClass = !message.read ? 'unread' : '';

    return `
        <div class="message-item ${unreadClass}" onclick="openMessage(${message.id})">
            <div class="message-item-icon ${message.type}">${icon}</div>
            <div class="message-item-content">
                <div class="message-item-header">
                    <h3 class="message-item-title">${escapeHtml(message.title)}</h3>
                    <span class="message-item-time">${time}</span>
                </div>
                <p class="message-item-preview">${escapeHtml(message.content)}</p>
            </div>
            <div class="message-item-actions">
                ${!message.read ? `<button onclick="event.stopPropagation(); markAsRead(${message.id})">标记已读</button>` : ''}
                <button class="btn-delete" onclick="event.stopPropagation(); deleteMessage(${message.id})">删除</button>
            </div>
        </div>
    `;
}

/**
 * 获取消息图标
 */
function getMessageIcon(type) {
    const icons = {
        'system': '🔔',
        'subscription': '📰',
        'profile': '👤',
        'announcement': '📢'
    };
    return icons[type] || '📬';
}

/**
 * 打开消息详情
 */
async function openMessage(messageId) {
    const message = allMessages.find(msg => msg.id === messageId);
    if (!message) return;

    currentMessageId = messageId;

    // 如果未读，标记为已读
    if (!message.read) {
        await markAsRead(messageId);
    }

    // 显示模态框
    document.getElementById('modalTitle').textContent = message.title;
    document.getElementById('modalType').textContent = getMessageTypeName(message.type);
    document.getElementById('modalType').className = `message-type ${message.type}`;
    document.getElementById('modalTime').textContent = formatTime(message.createdAt);
    document.getElementById('modalContent').textContent = message.content;

    document.getElementById('messageModal').style.display = 'flex';
}

/**
 * 关闭消息模态框
 */
function closeMessageModal() {
    document.getElementById('messageModal').style.display = 'none';
    currentMessageId = null;
}

/**
 * 标记消息为已读
 */
async function markAsRead(messageId) {
    const result = await messageAPI.markAsRead(messageId);
    if (result.success) {
        // 更新本地数据
        const message = allMessages.find(msg => msg.id === messageId);
        if (message) {
            message.read = true;
        }
        // 重新显示
        filterMessages(currentFilter);
        // 更新徽章
        updateMessageBadge();
    } else {
        showMessage('标记失败: ' + result.message, 'error');
    }
}

/**
 * 标记所有消息为已读
 */
async function markAllAsRead() {
    const result = await messageAPI.markAllAsRead();
    if (result.success) {
        // 更新本地数据
        allMessages.forEach(msg => msg.read = true);
        // 重新显示
        filterMessages(currentFilter);
        // 更新徽章
        updateMessageBadge();
        showMessage('已将所有消息标记为已读', 'success');
    } else {
        showMessage('操作失败: ' + result.message, 'error');
    }
}

/**
 * 删除消息
 */
async function deleteMessage(messageId) {
    const confirmed = confirm('确定要删除这条消息吗？');
    if (!confirmed) return;

    const result = await messageAPI.deleteMessage(messageId);
    if (result.success) {
        // 从本地数据中移除
        allMessages = allMessages.filter(msg => msg.id !== messageId);
        // 重新显示
        filterMessages(currentFilter);
        // 更新徽章
        updateMessageBadge();
        showMessage('消息已删除', 'success');
    } else {
        showMessage('删除失败: ' + result.message, 'error');
    }
}

/**
 * 删除当前查看的消息
 */
async function deleteCurrentMessage() {
    if (!currentMessageId) return;

    const confirmed = confirm('确定要删除这条消息吗？');
    if (!confirmed) return;

    const result = await messageAPI.deleteMessage(currentMessageId);
    if (result.success) {
        // 关闭模态框
        closeMessageModal();
        // 从本地数据中移除
        allMessages = allMessages.filter(msg => msg.id !== currentMessageId);
        // 重新显示
        filterMessages(currentFilter);
        // 更新徽章
        updateMessageBadge();
        showMessage('消息已删除', 'success');
    } else {
        showMessage('删除失败: ' + result.message, 'error');
    }
}

/**
 * 获取消息类型名称
 */
function getMessageTypeName(type) {
    const typeNames = {
        'system': '系统消息',
        'subscription': '订阅通知',
        'profile': '个人资料',
        'announcement': '系统公告'
    };
    return typeNames[type] || '消息';
}

/**
 * 格式化时间
 */
function formatTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;

    // 小于1分钟
    if (diff < 60 * 1000) {
        return '刚刚';
    }

    // 小于1小时
    if (diff < 60 * 60 * 1000) {
        const minutes = Math.floor(diff / (60 * 1000));
        return `${minutes}分钟前`;
    }

    // 今天
    if (date.toDateString() === now.toDateString()) {
        const hours = date.getHours();
        const minutes = date.getMinutes();
        return `今天 ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
    }

    // 昨天
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) {
        const hours = date.getHours();
        const minutes = date.getMinutes();
        return `昨天 ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;
    }

    // 显示完整日期
    return date.toLocaleString('zh-CN', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * 转义 HTML
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * 显示消息提示
 */
function showMessage(message, type) {
    // 简单的 alert 替代
    console.log(`[${type}] ${message}`);
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

// 点击模态框外部关闭
window.onclick = function(event) {
    const modal = document.getElementById('messageModal');
    if (event.target === modal) {
        closeMessageModal();
    }
};
