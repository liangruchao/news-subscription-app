#!/bin/bash

# ============================================================
# Docker 代理配置脚本
# 用途: 配置 Docker 通过 SOCKS 代理访问
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

SOCKS_PORT="1080"

# ============================================================
print_section "配置 Docker systemd 代理"

# 创建 systemd drop-in 目录
sudo mkdir -p /etc/systemd/system/docker.service.d

# 配置 HTTP/HTTPS 代理
sudo tee /etc/systemd/system/docker.service.d/http-proxy.conf > /dev/null << EOF
[Service]
Environment="HTTP_PROXY=socks5://127.0.0.1:$SOCKS_PORT"
Environment="HTTPS_PROXY=socks5://127.0.0.1:$SOCKS_PORT"
Environment="NO_PROXY=localhost,127.0.0.1"
EOF

log_success "Docker 代理配置已创建"

# ============================================================
print_section "重启 Docker"

log_info "重新加载 systemd 配置..."
sudo systemctl daemon-reload

log_info "重启 Docker 服务..."
sudo systemctl restart docker

log_success "Docker 服务已重启"

# ============================================================
print_section "验证配置"

log_info "检查 Docker 状态..."
sudo systemctl status docker --no-pager | head -5

echo ""
log_info "测试镜像拉取..."
echo "拉取 alpine 镜像..."

if timeout 60 docker pull alpine:latest > /dev/null 2>&1; then
    log_success "✓ 镜像拉取成功！"
else
    log_warning "镜像拉取测试失败"
    log_info "检查代理是否运行: ps aux | grep 'ssh -D $SOCKS_PORT'"
fi

# ============================================================
print_section "配置完成"

log_success "Docker 代理配置完成！"
echo ""
echo "代理配置:"
echo "  SOCKS5: 127.0.0.1:$SOCKS_PORT"
echo ""
echo "下一步:"
echo "  cd /var/www/news-app"
echo "  docker compose up -d"
echo ""
