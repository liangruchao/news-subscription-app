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
        alert('请选择新闻类别');
        return;
    }

    const result = await subscriptionAPI.addSubscription(category);

    if (result.success) {
        alert('订阅成功！');
        loadSubscriptions();
    } else {
        alert(result.message || '订阅失败');
    }
}

// 取消订阅
async function removeSubscription(category) {
    if (!confirm(`确定要取消${categoryNames[category] || category}的订阅吗？`)) {
        return;
    }

    const result = await subscriptionAPI.removeSubscription(category);

    if (result.success) {
        alert('取消订阅成功！');
        loadSubscriptions();
    } else {
        alert(result.message || '取消订阅失败');
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
