#!/bin/bash

# ============================================================
# GitHub SSH 密钥配置脚本
# 用途: 生成 SSH 密钥并添加到 GitHub
# ============================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_section() {
    echo ""
    echo "=================================================================="
    echo "  $1"
    echo "=================================================================="
    echo ""
}

# ============================================================
# 检查现有密钥
# ============================================================
print_section "检查 SSH 密钥"

if [ -f ~/.ssh/id_rsa ]; then
    log_warning "已存在 SSH 私钥"
    read -p "是否生成新的密钥? (yes/no): " GEN_NEW
    if [ "$GEN_NEW" != "yes" ]; then
        log_info "使用现有密钥"
        USE_EXISTING=true
    else
        USE_EXISTING=false
    fi
else
    USE_EXISTING=false
fi

# ============================================================
# 生成新的 SSH 密钥
# ============================================================
if [ "$USE_EXISTING" = false ]; then
    print_section "生成 SSH 密钥"

    log_info "生成新的 SSH 密钥对..."
    ssh-keygen -t rsa -b 4096 -C "root@$(hostname)-$(date +%Y%m%d)" -f ~/.ssh/id_rsa -N ""

    log_success "SSH 密钥已生成"
    echo ""
fi

# ============================================================
# 显示公钥
# ============================================================
print_section "GitHub SSH 公钥"

log_info "以下是你需要添加到 GitHub 的 SSH 公钥:"
echo ""
echo "=================================================================="
echo "请复制下面的公钥内容:"
echo "=================================================================="
cat ~/.ssh/id_rsa.pub
echo ""
echo "=================================================================="
echo ""

# ============================================================
# 配置 SSH Config
# ============================================================
print_section "配置 SSH"

cat >> ~/.ssh/config << 'EOF'

# GitHub 配置
Host github.com
    HostName github.com
    User git
    Port 22
    IdentityFile ~/.ssh/id_rsa
EOF

chmod 600 ~/.ssh/config

log_success "SSH 配置已更新"

# ============================================================
# 测试 SSH 连接
# ============================================================
print_section "测试 GitHub SSH 连接"

log_info "测试 SSH 连接到 GitHub..."
ssh -o StrictHostKeyChecking=no -T git@github.com 2>&1 | head -5 || echo "连接测试完成"

# ============================================================
# 配置 Git 使用 SSH 代理
# ============================================================
print_section "配置 Git 通过代理"

# 清除之前的代理设置
git config --global --unset http.proxy 2>/dev/null || true
git config --global --unset https.proxy 2>/dev/null || true

# 取消 Git 代理配置，因为我们使用 SSH
log_info "已清除 Git HTTP 代理配置"
log_info "Git 将使用 SSH 方式访问 GitHub（自动通过 SSH 隧道）"

# 测试 Git 访问
cd /tmp
rm -rf test-git-repo 2>/dev/null || true

log_info "测试 Git 克隆..."
if GIT_SSH_COMMAND="ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" git clone --depth 1 --quiet git@github.com:liangruchao/news-subscription-app.git test-git-repo 2>/dev/null; then
    log_success "✓ Git 可以访问 GitHub"
    rm -rf test-git-repo
else
    log_warning "Git 克隆测试失败"
    log_warning "请先将上面的公钥添加到 GitHub"
fi

# ============================================================
# 完成
# ============================================================
print_section "配置完成"

echo ""
echo "下一步操作:"
echo ""
echo "1. 复制上面显示的 SSH 公钥"
echo ""
echo "2. 添加到 GitHub:"
echo "   方式 A - Deploy Key (推荐):"
echo "   a. 访问: https://github.com/liangruchao/news-subscription-app/settings/keys"
echo "   b. 点击 'New deploy key'"
echo "   c. 粘贴公钥，添加标题 'Production VPS'"
echo "   d. 点击 'Add deploy key'"
echo ""
echo "   方式 B - 账户 SSH Key:"
echo "   a. 访问: https://github.com/settings/keys"
echo "   b. 点击 'New SSH key'"
echo "   c. 粘贴公钥，添加标题"
echo "   d. 点击 'Add SSH key'"
echo ""
echo "3. 添加完成后，运行以下命令测试:"
echo "   ssh -T git@github.com"
echo ""
echo "4. 如果成功，会显示:"
echo "   Hi liangruchao! You've successfully authenticated..."
echo ""
