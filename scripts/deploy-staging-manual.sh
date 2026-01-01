#!/bin/bash

# ============================================================
# Staging 环境手动部署脚本
# 用途: 在 Staging VPS 上手动部署应用
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
# 检查环境
# ============================================================
print_section "检查环境"

# 检查 Docker
if ! command -v docker &> /dev/null; then
    log_error "Docker 未安装"
    exit 1
fi
log_success "Docker 已安装"

# 检查 Docker Compose
if ! docker compose version &> /dev/null; then
    log_error "Docker Compose 未安装"
    exit 1
fi
log_success "Docker Compose 已安装"

# ============================================================
# 创建部署目录
# ============================================================
print_section "创建部署目录"

DEPLOY_DIR="/var/www/news-app"
mkdir -p $DEPLOY_DIR
cd $DEPLOY_DIR

log_success "部署目录: $DEPLOY_DIR"

# ============================================================
# 测试 GitHub 连接
# ============================================================
print_section "测试 GitHub 连接"

log_info "测试 SSH 连接到 GitHub..."
if ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 -T git@github.com 2>&1 | grep -q "successfully authenticated"; then
    log_success "✓ GitHub SSH 连接正常"
    GITHUB_ACCESSIBLE=true
else
    log_warning "GitHub SSH 连接失败"
    GITHUB_ACCESSIBLE=false
fi

# ============================================================
# 克隆或更新代码
# ============================================================
print_section "准备代码"

if [ "$GITHUB_ACCESSIBLE" = true ]; then
    if [ -d ".git" ]; then
        log_info "更新现有代码..."
        git fetch --all
        git reset --hard origin/develop
    else
        log_info "克隆代码仓库..."
        git clone -b develop git@github.com:liangruchao/news-subscription-app.git .
    fi
    log_success "代码已更新"
else
    log_error "无法访问 GitHub，请先配置 SSH 代理或密钥"
    log_info "运行以下命令配置:"
    echo "  bash scripts/configure-proxy-fixed.sh"
    echo "  bash scripts/configure-github-ssh.sh"
    exit 1
fi

# ============================================================
# 复制配置文件
# ============================================================
print_section "配置环境"

if [ -f "deploy/staging/.env" ]; then
    cp deploy/staging/.env .env
    log_success ".env 文件已配置"
else
    log_error "deploy/staging/.env 不存在"
    exit 1
fi

if [ -f "deploy/staging/docker-compose.yml" ]; then
    cp deploy/staging/docker-compose.yml docker-compose.yml
    log_success "docker-compose.yml 已配置"
else
    log_error "deploy/staging/docker-compose.yml 不存在"
    exit 1
fi

# 复制 nginx 配置
mkdir -p nginx/conf.d
if [ -f "deploy/staging/nginx/conf.d/default.conf" ]; then
    cp deploy/staging/nginx/conf.d/default.conf nginx/conf.d/default.conf
    log_success "Nginx 配置已复制"
fi

# ============================================================
# 构建镜像
# ============================================================
print_section "构建 Docker 镜像"

log_info "构建后端镜像..."
docker compose build backend

log_success "镜像构建完成"

# ============================================================
# 启动服务
# ============================================================
print_section "启动服务"

log_info "停止旧容器..."
docker compose down -v 2>/dev/null || true

log_info "启动新容器..."
docker compose up -d

log_success "服务启动完成"

# ============================================================
# 等待服务就绪
# ============================================================
print_section "等待服务就绪"

sleep 10

# 检查容器状态
log_info "容器状态:"
docker compose ps

# ============================================================
# 健康检查
# ============================================================
print_section "健康检查"

log_info "等待后端服务启动..."
for i in {1..30}; do
    if curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
        log_success "✓ 后端服务就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        log_warning "后端服务未就绪，请检查日志"
        log_info "运行: docker compose logs backend"
    fi
    sleep 2
done

log_info "检查 Nginx 状态..."
if curl -s http://localhost/ > /dev/null 2>&1; then
    log_success "✓ Nginx 就绪"
else
    log_warning "Nginx 未就绪"
fi

# ============================================================
# 完成
# ============================================================
print_section "部署完成"

log_success "Staging 环境部署完成！"
echo ""
echo "服务地址:"
echo "  Web:    http://47.103.204.114"
echo "  API:    http://47.103.204.114:8081"
echo "  Health: http://47.103.204.114:8081/actuator/health"
echo ""
echo "管理命令:"
echo "  查看日志: docker compose logs -f"
echo "  查看状态: docker compose ps"
echo "  重启服务: docker compose restart"
echo "  停止服务: docker compose down"
echo ""
