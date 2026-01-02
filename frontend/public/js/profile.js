// 当前用户数据
let currentProfile = null;

/**
 * 加载用户资料
 */
async function loadProfile() {
    const result = await profileAPI.getProfile();
    if (result.success) {
        currentProfile = result.data;
        displayProfile(currentProfile);
    } else {
        showMessage('profileMessage', result.message, 'error');
    }
}

/**
 * 显示用户资料
 */
function displayProfile(profile) {
    // 更新头像区域显示
    document.getElementById('usernameDisplay').textContent = profile.username;
    document.getElementById('emailDisplay').textContent = profile.email;

    // 更新表单
    document.getElementById('username').value = profile.username;
    document.getElementById('email').value = profile.email;
    document.getElementById('bio').value = profile.bio || '';

    // 更新头像
    if (profile.avatarUrl) {
        document.getElementById('avatarImage').src = profile.avatarUrl;
    }
}

/**
 * 更新用户资料
 */
async function updateProfile(event) {
    event.preventDefault();

    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const bio = document.getElementById('bio').value.trim();

    if (!username || !email) {
        showMessage('profileMessage', '用户名和邮箱不能为空', 'error');
        return;
    }

    const result = await profileAPI.updateProfile({ username, email, bio });
    if (result.success) {
        currentProfile = result.data;
        displayProfile(currentProfile);
        showMessage('profileMessage', '个人资料更新成功！', 'success');
    } else {
        showMessage('profileMessage', result.message, 'error');
    }
}

/**
 * 修改密码
 */
async function changePassword(event) {
    event.preventDefault();

    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        showMessage('passwordMessage', '两次输入的新密码不一致', 'error');
        return;
    }

    if (newPassword.length < 6) {
        showMessage('passwordMessage', '新密码长度至少为6位', 'error');
        return;
    }

    const result = await profileAPI.changePassword(oldPassword, newPassword);
    if (result.success) {
        showMessage('passwordMessage', '密码修改成功！请重新登录', 'success');
        document.getElementById('passwordForm').reset();
        // 2秒后自动退出登录
        setTimeout(() => {
            logout();
        }, 2000);
    } else {
        showMessage('passwordMessage', result.message, 'error');
    }
}

/**
 * 上传头像
 */
async function uploadAvatar(file) {
    if (!file) return;

    // 检查文件类型
    if (!file.type.startsWith('image/')) {
        alert('请选择图片文件');
        return;
    }

    // 检查文件大小（最大2MB）
    if (file.size > 2 * 1024 * 1024) {
        alert('图片大小不能超过2MB');
        return;
    }

    // 显示上传进度
    const uploadBtn = document.querySelector('.avatar-upload-btn');
    const originalText = uploadBtn.textContent;
    uploadBtn.textContent = '上传中...';
    uploadBtn.disabled = true;

    try {
        // 使用 FormData 上传文件
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${API_BASE_URL}/avatar`, {
            method: 'POST',
            credentials: 'include',
            body: formData,
        });

        const result = await response.json();

        if (result.success) {
            // 更新头像显示
            if (result.data && result.data.avatarUrl) {
                document.getElementById('avatarImage').src = result.data.avatarUrl;
            } else {
                // 刷新个人资料获取新头像
                await loadProfile();
            }
            alert('头像上传成功！');
        } else {
            alert(result.message || '头像上传失败');
        }
    } catch (error) {
        console.error('头像上传失败:', error);
        alert('头像上传失败，请稍后重试');
    } finally {
        uploadBtn.textContent = originalText;
        uploadBtn.disabled = false;
        // 清空文件输入
        document.getElementById('avatarUpload').value = '';
    }
}

/**
 * 注销账户
 */
async function deleteAccount() {
    const confirmed = confirm('警告：此操作将永久删除您的账户和所有数据，且无法恢复！\n\n您确定要注销账户吗？');
    if (!confirmed) return;

    const doubleConfirmed = prompt('为了确认，请输入 "DELETE" 来继续注销账户：');
    if (doubleConfirmed !== 'DELETE') {
        alert('已取消注销账户操作');
        return;
    }

    const result = await profileAPI.deleteAccount();
    if (result.success) {
        alert('账户已成功注销');
        window.location.href = 'index.html';
    } else {
        alert(result.message || '注销账户失败');
    }
}

/**
 * 加载统计数据
 */
async function loadStatistics() {
    const result = await statisticsAPI.getStatistics();
    if (result.success) {
        const stats = result.data;
        document.getElementById('subscriptionCount').textContent = stats.subscriptionCount || 0;

        // 计算注册天数
        if (currentProfile && currentProfile.createdAt) {
            const days = Math.floor((Date.now() - new Date(currentProfile.createdAt)) / (1000 * 60 * 60 * 24));
            document.getElementById('daysSinceRegister').textContent = days;
        }
    }
}

/**
 * 加载登录历史
 */
async function loadLoginHistory() {
    const result = await loginHistoryAPI.getLoginHistory();
    if (result.success) {
        const historyList = document.getElementById('loginHistoryList');
        const history = result.data || [];

        if (history.length === 0) {
            historyList.innerHTML = '<p style="color: #999; text-align: center;">暂无登录记录</p>';
            return;
        }

        historyList.innerHTML = history.map((item, index) => `
            <div class="login-history-item">
                <div class="login-history-info">
                    <span class="login-history-time">${formatDateTime(item.loginTime)}</span>
                    ${item.ipAddress ? `<span class="login-history-ip">IP: ${item.ipAddress}</span>` : ''}
                </div>
                ${index === 0 ? '<span class="login-history-current">当前</span>' : ''}
            </div>
        `).join('');
    }
}

/**
 * 显示消息提示
 */
function showMessage(elementId, message, type) {
    const messageEl = document.getElementById(elementId);
    messageEl.textContent = message;
    messageEl.className = `message ${type}`;
    messageEl.style.display = 'block';

    // 3秒后自动隐藏
    setTimeout(() => {
        messageEl.style.display = 'none';
    }, 3000);
}

/**
 * 格式化日期时间
 */
function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    const now = new Date();
    const diff = now - date;

    // 小于1小时
    if (diff < 60 * 60 * 1000) {
        const minutes = Math.floor(diff / (60 * 1000));
        return minutes <= 1 ? '刚刚' : `${minutes}分钟前`;
    }

    // 小于24小时
    if (diff < 24 * 60 * 60 * 1000) {
        const hours = Math.floor(diff / (60 * 60 * 1000));
        return `${hours}小时前`;
    }

    // 小于7天
    if (diff < 7 * 24 * 60 * 60 * 1000) {
        const days = Math.floor(diff / (24 * 60 * 60 * 1000));
        return `${days}天前`;
    }

    // 显示完整日期
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * 更新消息徽章（供其他页面调用）
 */
async function updateMessageBadge() {
    const result = await messageAPI.getUnreadCount();
    if (result.success && result.data > 0) {
        const badge = document.getElementById('messageBadge');
        if (badge) {
            badge.textContent = result.data > 99 ? '99+' : result.data;
        }
    }
}
