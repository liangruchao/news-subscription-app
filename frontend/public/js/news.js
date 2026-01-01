// 类别名称映射
const categoryNames = {
    business: '商业',
    entertainment: '娱乐',
    general: '综合',
    health: '健康',
    science: '科学',
    sports: '体育',
    technology: '科技',
};

// 显示消息（与 auth.js 中的函数类似）
function showNewsMessage(message, type = 'success') {
    // 创建消息元素
    const messageDiv = document.createElement('div');
    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 12px 24px;
        background: ${type === 'success' ? '#10b981' : '#ef4444'};
        color: white;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    `;

    // 添加动画样式
    if (!document.getElementById('messageAnimation')) {
        const style = document.createElement('style');
        style.id = 'messageAnimation';
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(messageDiv);

    // 3秒后自动移除
    setTimeout(() => {
        messageDiv.style.animation = 'slideIn 0.3s ease-out reverse';
        setTimeout(() => messageDiv.remove(), 300);
    }, 3000);
}

// 加载订阅列表
async function loadSubscriptions() {
    const result = await subscriptionAPI.getSubscriptions();
    const subscriptionList = document.getElementById('subscriptionList');

    if (result.success && result.data) {
        subscriptionList.innerHTML = result.data.map(sub => `
            <div class="subscription-item">
                <span>${categoryNames[sub.category] || sub.category}</span>
                <button onclick="removeSubscription('${sub.category}')" title="取消订阅">×</button>
            </div>
        `).join('');
    } else {
        subscriptionList.innerHTML = '<p style="color: #999;">暂无订阅</p>';
    }
}

// 添加订阅
async function addSubscription() {
    const select = document.getElementById('categorySelect');
    const category = select.value;

    if (!category) {
        showNewsMessage('请选择新闻类别', 'error');
        return;
    }

    const result = await subscriptionAPI.addSubscription(category);

    if (result.success) {
        showNewsMessage(`订阅${categoryNames[category] || category}成功！`);
        loadSubscriptions();
    } else {
        showNewsMessage(result.message || '订阅失败', 'error');
    }
}

// 取消订阅
async function removeSubscription(category) {
    // 使用 confirm，但改进提示文本
    if (!confirm(`确定要取消"${categoryNames[category] || category}"的订阅吗？`)) {
        return;
    }

    const result = await subscriptionAPI.removeSubscription(category);

    if (result.success) {
        showNewsMessage(`取消订阅${categoryNames[category] || category}成功！`);
        loadSubscriptions();
    } else {
        showNewsMessage(result.message || '取消订阅失败', 'error');
    }
}

// 加载新闻
async function loadNews() {
    const newsList = document.getElementById('newsList');
    newsList.innerHTML = '<p style="text-align: center; color: #999;">加载中...</p>';

    const result = await newsAPI.getUserNews();

    if (result.success && result.data && result.data.length > 0) {
        newsList.innerHTML = result.data.map(news => `
            <div class="news-item">
                ${news.urlToImage ? `<img src="${news.urlToImage}" alt="${news.title}" onerror="this.style.display='none'">` : ''}
                <div class="news-item-content">
                    <div class="news-item-title">${news.title || '无标题'}</div>
                    <div class="news-item-description">${news.description || '暂无描述'}</div>
                    <div class="news-item-meta">
                        <span class="news-item-source">${news.source || '未知来源'}</span>
                        <a href="${news.url}" target="_blank" class="news-item-link">阅读更多 →</a>
                    </div>
                </div>
            </div>
        `).join('');
    } else if (result.message && result.message.includes('订阅')) {
        newsList.innerHTML = '<p style="text-align: center; color: #999;">请先订阅新闻类别</p>';
    } else {
        newsList.innerHTML = '<p style="text-align: center; color: #999;">暂无新闻或获取失败</p>';
    }
}

