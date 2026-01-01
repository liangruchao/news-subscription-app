#!/bin/bash

# ============================================================
# Production VPS SSH 代理配置脚本 (修复版)
# 代理服务器: 206.237.4.197:22
# 用户: root
# ============================================================

set -e

# ============================================================
# 配置
# ============================================================
PROXY_SERVER="206.237.4.197"
PROXY_USER="root"
PROXY_PORT="22"
SOCKS_PORT="1080"

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

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_section() {
    echo ""
    echo "=================================================================="
    echo "  $1"
    echo "=================================================================="
    echo ""
}

# ============================================================
# 修复 APT 源问题
# ============================================================
print_section "修复 APT 源"

log_info "删除有问题的 Docker 源..."
rm -f /etc/apt/sources.list.d/download.docker.list

log_info "更新软件源列表..."
apt-get update -qq

log_success "APT 源已修复"

# ============================================================
# 检查并安装必要工具
# ============================================================
print_section "检查必要工具"

# 检查 sshpass
if ! command -v sshpass &> /dev/null; then
    log_info "安装 sshpass (用于密码认证)..."
    DEBIAN_FRONTEND=noninteractive apt-get install -y sshpass
    log_success "sshpass 安装完成"
else
    log_success "sshpass 已安装"
fi

# 检查 nc (netcat)
if ! command -v nc &> /dev/null; then
    log_info "安装 netcat..."
    DEBIAN_FRONTEND=noninteractive apt-get install -y netcat
    log_success "netcat 安装完成"
else
    log_success "netcat 已安装"
fi

# ============================================================
# 创建 SOCKS 隧道启动脚本
# ============================================================
print_section "创建 SOCKS 隧道脚本"

cat > ~/start-ssh-tunnel.sh << 'EOF'
#!/bin/bash

# 启动 SSH SOCKS 隧道
PROXY_SERVER="206.237.4.197"
PROXY_USER="root"
PROXY_PORT="22"
SOCKS_PORT="1080"

# 检查是否已在运行
if pgrep -f "ssh -D $SOCKS_PORT.*$PROXY_SERVER" > /dev/null; then
    echo "SSH 隧道已在运行"
    exit 0
fi

echo "启动 SSH 隧道..."
echo "代理服务器: $PROXY_USER@$PROXY_SERVER:$PROXY_PORT"
echo "SOCKS 端口: $SOCKS_PORT"

# 使用 sshpass 启动隧道（后台运行）
sshpass -p 'XpF5d99dGThaixM' ssh -D $SOCKS_PORT -N -f -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null $PROXY_USER@$PROXY_SERVER

if [ $? -eq 0 ]; then
    echo "✓ SSH 隧道已启动"
    echo "  SOCKS 代理: socks5://127.0.0.1:$SOCKS_PORT"
else
    echo "✗ SSH 隧道启动失败"
    exit 1
fi
EOF

chmod +x ~/start-ssh-tunnel.sh

log_success "SOCKS 隧道脚本已创建: ~/start-ssh-tunnel.sh"

# ============================================================
# 创建停止隧道脚本
# ============================================================
cat > ~/stop-ssh-tunnel.sh << 'EOF'
#!/bin/bash

SOCKS_PORT="1080"
PROXY_SERVER="206.237.4.197"

echo "停止 SSH 隧道..."
pkill -f "ssh -D $SOCKS_PORT.*$PROXY_SERVER"
echo "✓ SSH 隧道已停止"
EOF

chmod +x ~/stop-ssh-tunnel.sh

log_success "停止隧道脚本已创建: ~/stop-ssh-tunnel.sh"

# ============================================================
# 配置 Git 使用代理
# ============================================================
print_section "配置 Git"

# 备份现有 Git 配置
if [ -f ~/.gitconfig ]; then
    cp ~/.gitconfig ~/.gitconfig.backup.$(date +%Y%m%d%H%M%S)
fi

git config --global http.proxy socks5://127.0.0.1:1080
git config --global https.proxy socks5://127.0.0.1:1080

log_success "Git 代理配置完成"
echo "  http.proxy: socks5://127.0.0.1:1080"
echo "  https.proxy: socks5://127.0.0.1:1080"

# ============================================================
# 启动 SSH 隧道
# ============================================================
print_section "启动 SSH 隧道"

log_info "启动 SSH 隧道..."
~/start-ssh-tunnel.sh

sleep 3

# ============================================================
# 测试代理连接
# ============================================================
print_section "测试代理连接"

log_info "测试 GitHub API 访问..."
HTTP_CODE=$(curl -x socks5://127.0.0.1:1080 -s --max-time 10 -o /dev/null -w "%{http_code}" https://api.github.com 2>/dev/null || echo "000")

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
    log_success "✓ 可以通过代理访问 GitHub API (HTTP $HTTP_CODE)"
else
    log_warning "访问 GitHub API 失败 (HTTP $HTTP_CODE)，可能是隧道尚未就绪"
    log_info "等待几秒后再测试..."
    sleep 5

    HTTP_CODE=$(curl -x socks5://127.0.0.1:1080 -s --max-time 10 -o /dev/null -w "%{http_code}" https://api.github.com 2>/dev/null || echo "000")
    if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
        log_success "✓ 第二次测试成功 (HTTP $HTTP_CODE)"
    else
        log_error "无法通过代理访问 GitHub API"
    fi
fi

log_info "测试 Git 访问 GitHub..."
cd /tmp
rm -rf test-git-repo 2>/dev/null || true

if git clone --depth 1 --quiet git@github.com:liangruchao/news-subscription-app.git test-git-repo 2>/dev/null; then
    log_success "✓ Git 可以通过代理访问 GitHub"
    rm -rf test-git-repo
else
    log_warning "Git 访问 GitHub 失败，可能需要配置 SSH 密钥"
    log_info "提示: Git SSH 连接使用的是 ~/.ssh/config 中的 ProxyCommand 配置"
fi

# ============================================================
# 创建隧道状态检查脚本
# ============================================================
cat > ~/tunnel-status.sh << 'EOF'
#!/bin/bash

SOCKS_PORT="1080"
PROXY_SERVER="206.237.4.197"

echo "=================================================================="
echo "  SSH 隧道状态"
echo "=================================================================="
echo ""
echo "代理服务器: root@$PROXY_SERVER"
echo "SOCKS 端口:  $SOCKS_PORT"
echo ""

if pgrep -f "ssh -D $SOCKS_PORT.*$PROXY_SERVER" > /dev/null; then
    PID=$(pgrep -f "ssh -D $SOCKS_PORT.*$PROXY_SERVER")
    echo "状态: 运行中 ✓ (PID: $PID)"
    echo ""
    echo "网络连接:"
    ss -tlnp 2>/dev/null | grep ":$SOCKS_PORT" || netstat -tlnp 2>/dev/null | grep ":$SOCKS_PORT"
else
    echo "状态: 未运行"
    echo ""
    echo "启动命令: ~/start-ssh-tunnel.sh"
fi
echo ""

# 测试代理
if curl -x socks5://127.0.0.1:$SOCKS_PORT -s --max-time 5 https://api.github.com > /dev/null 2>&1; then
    echo "代理连接: 正常 ✓"
else
    echo "代理连接: 不可用 ✗"
fi
echo ""
EOF

chmod +x ~/tunnel-status.sh

# ============================================================
# 创建开机自启动服务 (systemd)
# ============================================================
print_section "配置开机自启动"

cat > /etc/systemd/system/ssh-tunnel.service << 'EOF'
[Unit]
Description=SSH Tunnel to Proxy Server
After=network.target

[Service]
Type=forking
User=root
ExecStart=/root/start-ssh-tunnel.sh
ExecStop=/root/stop-ssh-tunnel.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable ssh-tunnel.service

log_success "开机自启动服务已配置"

# ============================================================
# 完成
# ============================================================
print_section "配置完成"

log_success "SSH 代理配置完成！"
echo ""
echo "可用命令:"
echo "  ~/start-ssh-tunnel.sh   - 启动 SSH 隧道"
echo "  ~/stop-ssh-tunnel.sh    - 停止 SSH 隧道"
echo "  ~/tunnel-status.sh      - 查看隧道状态"
echo ""
echo "系统服务:"
echo "  systemctl start ssh-tunnel   - 启动隧道服务"
echo "  systemctl stop ssh-tunnel    - 停止隧道服务"
echo "  systemctl status ssh-tunnel  - 查看服务状态"
echo ""
echo "Git 代理配置:"
git config --global --get http.proxy
git config --global --get https.proxy
echo ""
echo "取消 Git 代理 (如果需要):"
echo "  git config --global --unset http.proxy"
echo "  git config --global --unset https.proxy"
echo ""
echo "下一步: 运行部署脚本"
echo "  ./scripts/deploy-to-production.sh"
echo ""
