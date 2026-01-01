// 公告列表数据
let allAnnouncements = [];

/**
 * 加载公告列表
 */
async function loadAnnouncements() {
    const result = await announcementAPI.getPublishedAnnouncements();
    if (result.success) {
        allAnnouncements = result.data || [];
        displayAnnouncements();
    } else {
        console.error('加载公告失败:', result.message);
    }
}

/**
 * 显示公告列表
 */
function displayAnnouncements() {
    const announcementsList = document.getElementById('announcementsList');
    const emptyState = document.getElementById('emptyState');

    if (allAnnouncements.length === 0) {
        announcementsList.innerHTML = '';
        emptyState.style.display = 'block';
        return;
    }

    emptyState.style.display = 'none';
    announcementsList.innerHTML = allAnnouncements.map(announcement => createAnnouncementItem(announcement)).join('');
}

/**
 * 创建公告项
 */
function createAnnouncementItem(announcement) {
    const priority = announcement.priority || 'medium';
    const priorityClass = `priority-${priority}`;
    const priorityLabel = getPriorityLabel(priority);
    const time = formatTime(announcement.publishedAt);

    return `
        <div class="announcement-item ${priorityClass}" onclick="openAnnouncement(${announcement.id})">
            <div class="announcement-item-header">
                <h3 class="announcement-item-title">${escapeHtml(announcement.title)}</h3>
                <span class="announcement-item-priority ${priority}">${priorityLabel}</span>
            </div>
            <div class="announcement-item-meta">
                <span class="announcement-item-time">📅 ${time}</span>
                ${announcement.authorName ? `<span class="announcement-item-author">👤 ${escapeHtml(announcement.authorName)}</span>` : ''}
            </div>
            <p class="announcement-item-preview">${escapeHtml(announcement.content)}</p>
        </div>
    `;
}

/**
 * 打开公告详情
 */
function openAnnouncement(announcementId) {
    const announcement = allAnnouncements.find(a => a.id === announcementId);
    if (!announcement) return;

    // 显示模态框
    document.getElementById('modalTitle').textContent = announcement.title;

    const priority = announcement.priority || 'medium';
    const priorityLabel = getPriorityLabel(priority);
    document.getElementById('modalPriority').textContent = `优先级：${priorityLabel}`;
    document.getElementById('modalPriority').className = `announcement-priority ${priority}`;
    document.getElementById('modalTime').textContent = `发布时间：${formatTime(announcement.publishedAt)}`;
    document.getElementById('modalContent').textContent = announcement.content;

    document.getElementById('announcementModal').style.display = 'flex';
}

/**
 * 关闭公告模态框
 */
function closeAnnouncementModal() {
    document.getElementById('announcementModal').style.display = 'none';
}

/**
 * 获取优先级标签
 */
function getPriorityLabel(priority) {
    const labels = {
        'high': '重要',
        'medium': '普通',
        'low': '提示'
    };
    return labels[priority] || '普通';
}

/**
 * 格式化时间
 */
function formatTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;

    // 小于1小时
    if (diff < 60 * 60 * 1000) {
        const minutes = Math.floor(diff / (60 * 1000));
        return minutes <= 1 ? '刚刚发布' : `${minutes}分钟前`;
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

    // 本周
    const weekAgo = new Date(now);
    weekAgo.setDate(weekAgo.getDate() - 7);
    if (date > weekAgo) {
        const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        return days[date.getDay()];
    }

    // 显示完整日期
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
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
    const modal = document.getElementById('announcementModal');
    if (event.target === modal) {
        closeAnnouncementModal();
    }
};
