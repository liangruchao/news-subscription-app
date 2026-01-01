#!/bin/bash

# ============================================================
# Docker 镜像加速配置脚本
# 用途: 配置国内 Docker 镜像加速器
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
print_section "配置 Docker 镜像加速"

# 创建 Docker 配置目录
sudo mkdir -p /etc/docker

# 配置镜像加速器（使用多个国内镜像源）
sudo tee /etc/docker/daemon.json > /dev/null << 'EOF'
{
  "registry-mirrors": [
    "https://docker.1panel.live",
    "https://docker.unsee.tech",
    "https://docker.ckyl.me",
    "https://docker.awsl9527.cn"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
EOF

log_success "Docker 配置已更新"

# ============================================================
print_section "重启 Docker"

log_info "重启 Docker 服务..."
sudo systemctl daemon-reload
sudo systemctl restart docker

sleep 3

log_success "Docker 服务已重启"

# ============================================================
print_section "验证配置"

log_info "检查 Docker 状态..."
sudo systemctl status docker --no-pager | head -5

echo ""
log_info "测试镜像拉取..."
echo "拉取 hello-world 镜像..."

if sudo docker pull hello-world > /dev/null 2>&1; then
    log_success "✓ 镜像拉取成功！"
else
    log_warning "镜像拉取测试失败，但配置已完成"
fi

# ============================================================
print_section "配置完成"

log_success "Docker 镜像加速配置完成！"
echo ""
echo "镜像源列表:"
echo "  - https://docker.1panel.live"
echo "  - https://docker.unsee.tech"
echo "  - https://docker.ckyl.me"
echo "  - https://docker.awsl9527.cn"
echo ""
echo "下一步:"
echo "  cd /var/www/news-app/news-subscription-app"
echo "  docker compose up -d"
echo ""
