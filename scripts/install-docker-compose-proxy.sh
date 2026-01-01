#!/bin/bash

# ============================================================
# 通过代理安装 Docker Compose
# 用途: 在有网络限制的 VPS 上安装 Docker Compose
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
# 修复 APT 源问题（同 Production VPS）
# ============================================================
print_section "修复 APT 源"

log_info "删除有问题的 Docker 源..."
sudo rm -f /etc/apt/sources.list.d/download.docker.list

log_info "更新软件源列表..."
sudo apt-get update -qq

log_success "APT 源已修复"

# ============================================================
# 检查并安装必要工具
# ============================================================
print_section "检查必要工具"

# 检查 sshpass
if ! command -v sshpass &> /dev/null; then
    log_info "安装 sshpass..."
    sudo DEBIAN_FRONTEND=noninteractive apt-get install -y sshpass
    log_success "sshpass 安装完成"
else
    log_success "sshpass 已安装"
fi

# ============================================================
# 方法 1: 使用 GitHub Releases 下载（推荐）
# ============================================================
print_section "安装 Docker Compose (方法 1: 直接下载)"

# 获取最新版本
COMPOSE_VERSION="2.35.1"

log_info "下载 Docker Compose 插件..."

# 尝试通过代理下载（如果 SSH 隧道已运行）
if curl -x socks5://127.0.0.1:1080 -s --head https://github.com | head -n 1 | grep "200" > /dev/null; then
    log_info "通过代理下载..."

    # 下载 Docker Compose 插件
    curl -x socks5://127.0.0.1:1080 -SL \
        "https://github.com/docker/compose/releases/download/v${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
        -o /tmp/docker-compose

    # 安装
    sudo mkdir -p /usr/local/lib/docker/cli-plugins
    sudo mv /tmp/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose
    sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    log_success "Docker Compose 安装完成"

elif curl -s --head https://get.docker.com | head -n 1 | grep "200" > /dev/null; then
    log_info "直接下载..."

    # 下载 Docker Compose 插件
    curl -SL \
        "https://github.com/docker/compose/releases/download/v${COMPOSE_VERSION}/docker-compose-linux-x86_64" \
        -o /tmp/docker-compose

    # 安装
    sudo mkdir -p /usr/local/lib/docker/cli-plugins
    sudo mv /tmp/docker-compose /usr/local/lib/docker/cli-plugins/docker-compose
    sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    log_success "Docker Compose 安装完成"

else
    log_warning "无法直接下载，尝试方法 2..."

    # ============================================================
    # 方法 2: 使用 Python pip 安装
    # ============================================================
    print_section "安装 Docker Compose (方法 2: 使用 pip)"

    # 安装 Python 和 pip
    sudo apt-get install -y python3 python3-pip

    # 通过 pip 安装 docker-compose（旧版本但可用）
    pip3 install --break-system-packages docker-compose

    log_success "Docker Compose (standalone) 已安装"
fi

# ============================================================
# 验证安装
# ============================================================
print_section "验证安装"

log_info "检查 Docker Compose 版本..."

if docker compose version &> /dev/null; then
    docker compose version
    log_success "✓ Docker Compose 插件已安装"
elif docker-compose --version &> /dev/null; then
    docker-compose --version
    log_success "✓ Docker Compose standalone 已安装"
    log_info "注意: 使用 'docker-compose' 而不是 'docker compose'"
else
    log_error "Docker Compose 安装失败"
    exit 1
fi

# ============================================================
# 完成
# ============================================================
print_section "安装完成"

log_success "Docker Compose 安装完成！"
echo ""
echo "下一步:"
echo "  cd /var/www/news-app"
echo "  bash scripts/deploy-staging-manual.sh"
echo ""
