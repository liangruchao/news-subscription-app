/**
 * 加载状态指示器组件
 */

// 全局加载状态
let loadingCount = 0;
let loadingElement = null;

/**
 * 显示加载指示器
 * @param {string} message - 加载提示文字（可选）
 */
function showLoading(message = '加载中...') {
    loadingCount++;

    // 如果已经显示，只更新计数
    if (loadingElement) {
        updateLoadingMessage(message);
        return;
    }

    // 创建加载指示器
    loadingElement = document.createElement('div');
    loadingElement.id = 'globalLoading';
    loadingElement.innerHTML = `
        <div class="loading-overlay">
            <div class="loading-spinner"></div>
            <div class="loading-text">${message}</div>
        </div>
    `;

    // 添加样式
    if (!document.getElementById('loadingStyles')) {
        const style = document.createElement('style');
        style.id = 'loadingStyles';
        style.textContent = `
            .loading-overlay {
                position: fixed;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                z-index: 9999;
                animation: fadeIn 0.2s ease-out;
            }

            .loading-spinner {
                width: 50px;
                height: 50px;
                border: 4px solid rgba(255, 255, 255, 0.3);
                border-top-color: #667eea;
                border-radius: 50%;
                animation: spin 0.8s linear infinite;
            }

            .loading-text {
                margin-top: 16px;
                color: white;
                font-size: 16px;
                font-weight: 500;
            }

            @keyframes spin {
                to { transform: rotate(360deg); }
            }

            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }

            @keyframes fadeOut {
                from { opacity: 1; }
                to { opacity: 0; }
            }
        `;
        document.head.appendChild(style);
    }

    document.body.appendChild(loadingElement);
    document.body.style.overflow = 'hidden'; // 禁止滚动
}

/**
 * 隐藏加载指示器
 */
function hideLoading() {
    loadingCount--;

    // 如果还有其他加载任务，不隐藏
    if (loadingCount > 0) {
        return;
    }

    if (loadingElement) {
        loadingElement.style.animation = 'fadeOut 0.2s ease-out';
        setTimeout(() => {
            if (loadingElement && loadingCount <= 0) {
                loadingElement.remove();
                loadingElement = null;
                document.body.style.overflow = ''; // 恢复滚动
            }
        }, 200);
    }
}

/**
 * 更新加载提示文字
 * @param {string} message - 新的提示文字
 */
function updateLoadingMessage(message) {
    if (loadingElement) {
        const textElement = loadingElement.querySelector('.loading-text');
        if (textElement) {
            textElement.textContent = message;
        }
    }
}

/**
 * 带加载状态的异步执行包装器
 * @param {Promise} promise - 要执行的 Promise
 * @param {string} message - 加载提示文字
 * @returns {Promise} - 原始 Promise
 */
async function withLoading(promise, message = '加载中...') {
    showLoading(message);
    try {
        const result = await promise;
        return result;
    } finally {
        hideLoading();
    }
}
