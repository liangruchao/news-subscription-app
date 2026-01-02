// 显示消息
function showMessage(message, type = 'success') {
    const messageDiv = document.getElementById('message');
    if (messageDiv) {
        messageDiv.textContent = message;
        messageDiv.className = `message ${type}`;
        messageDiv.style.display = 'block';

        // 3秒后自动隐藏
        setTimeout(() => {
            messageDiv.style.display = 'none';
        }, 3000);
    }
}

// 处理登录
async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    const result = await authAPI.login(username, password);

    if (result.success) {
        showMessage('登录成功！正在跳转...', 'success');
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);
    } else {
        showMessage(result.message || '登录失败', 'error');
    }
}

// 处理注册
async function handleRegister(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // 验证密码
    if (password !== confirmPassword) {
        showMessage('两次输入的密码不一致', 'error');
        return;
    }

    if (password.length < 6) {
        showMessage('密码长度至少为6位', 'error');
        return;
    }

    const result = await authAPI.register(username, email, password);

    if (result.success) {
        showMessage('注册成功！正在跳转...', 'success');
        setTimeout(() => {
            window.location.href = 'index.html';
        }, 1000);
    } else {
        showMessage(result.message || '注册失败', 'error');
    }
}

// 检查登录状态
async function checkLoginStatus() {
    const result = await authAPI.getCurrentUser();
    const welcomeSection = document.getElementById('welcomeSection');
    const loggedInSection = document.getElementById('loggedInSection');
    const userInfo = document.getElementById('userInfo');
    const mainNav = document.getElementById('mainNav');

    if (result.success && result.data) {
        // 已登录
        if (welcomeSection) welcomeSection.style.display = 'none';
        if (loggedInSection) loggedInSection.style.display = 'block';
        if (userInfo) userInfo.style.display = 'none';
        if (mainNav) mainNav.style.display = 'block';

        // 更新导航栏用户信息
        updateNavUserInfo(result.data);

        // 更新消息徽章
        updateMessageBadge();

        return true;
    } else {
        // 未登录
        if (welcomeSection) welcomeSection.style.display = 'block';
        if (loggedInSection) loggedInSection.style.display = 'none';
        if (userInfo) userInfo.style.display = 'block';
        if (mainNav) mainNav.style.display = 'none';
        return false;
    }
}

// 更新导航栏用户信息
function updateNavUserInfo(user) {
    const navUsername = document.getElementById('navUsername');
    const navAvatar = document.getElementById('navAvatar');

    if (navUsername) navUsername.textContent = user.username;
    if (navAvatar && user.avatarUrl) {
        navAvatar.src = user.avatarUrl;
    }
}

// 处理登出
async function handleLogout() {
    const result = await authAPI.logout();
    if (result.success) {
        window.location.href = 'login.html';
    }
}

// 退出登录（统一函数）
async function logout() {
    const result = await authAPI.logout();
    if (result.success) {
        window.location.href = 'login.html';
    }
}

// 检查是否登录（用于其他页面）
// 这是一个异步函数，需要使用 await 调用
async function isLoggedIn() {
    try {
        const result = await authAPI.getCurrentUser();
        return result.success && result.data;
    } catch (error) {
        console.error('检查登录状态失败:', error);
        return false;
    }
}

// 更新消息徽章
async function updateMessageBadge() {
    try {
        const result = await messageAPI.getUnreadCount();
        const badge = document.getElementById('messageBadge');
        if (badge) {
            if (result.success && result.data > 0) {
                badge.textContent = result.data > 99 ? '99+' : result.data;
            } else {
                badge.textContent = '';
            }
        }
    } catch (error) {
        console.error('更新消息徽章失败:', error);
    }
}

