#!/bin/bash

# ============================================================
# 前端部署脚本
# 用途: 将前端静态文件部署到 VPS
# 使用: ./scripts/deploy-frontend.sh
# ============================================================

set -e

# ============================================================
# 配置
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
FRONTEND_DIR="${PROJECT_ROOT}/frontend/public"

# VPS 配置
VPS_HOST="47.103.204.114"
VPS_USER="root"
VPS_FRONTEND_DIR="/var/www/news-app/frontend"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
print_section() { echo ""; echo "=================================================================="; echo "  $1"; echo "=================================================================="; echo ""; }

# ============================================================
# 1. 检查前端文件
# ============================================================
print_section "1. 检查前端文件"

if [ ! -d "$FRONTEND_DIR" ]; then
    log_error "前端目录不存在: $FRONTEND_DIR"
    exit 1
fi

log_info "前端文件检查通过"
ls -lh "$FRONTEND_DIR"

# ============================================================
# 2. 创建 VPS 前端目录
# ============================================================
print_section "2. 创建 VPS 前端目录"

log_info "在 VPS 上创建前端目录..."
ssh ${VPS_USER}@${VPS_HOST} << ENDSSH
# 创建前端目录
mkdir -p ${VPS_FRONTEND_DIR}

# 设置权限
chown -R newsapp:newsapp /var/www/news-app
chmod -R 755 /var/www/news-app/frontend

echo "前端目录创建完成"
ls -la /var/www/news-app/
ENDSSH

log_success "前端目录创建完成"

# ============================================================
# 3. 上传前端文件
# ============================================================
print_section "3. 上传前端文件"

log_info "上传前端静态文件到 VPS..."
rsync -avz --delete \
    --exclude 'node_modules' \
    --exclude '.git' \
    --exclude 'test' \
    "${FRONTEND_DIR}/" \
    ${VPS_USER}@${VPS_HOST}:${VPS_FRONTEND_DIR}/

log_success "前端文件上传完成"

# ============================================================
# 4. 更新 Nginx 配置
# ============================================================
print_section "4. 更新 Nginx 配置"

log_info "创建前端 Nginx 配置..."
ssh ${VPS_USER}@${VPS_HOST} << 'ENDNGINX'

# 创建新的 Nginx 配置（包含前端和后端）
cat > /etc/nginx/sites-available/news-app-full << 'EOF'
# 前端静态文件服务
server {
    listen 80;
    server_name 47.103.204.114;

    # 前端根目录
    root /var/www/news-app/frontend;
    index index.html;

    # 日志
    access_log /var/log/nginx/news-app-access.log;
    error_log /var/log/nginx/news-app-error.log;

    # 前端静态文件
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 限制请求体大小
    client_max_body_size 10M;

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json;
}
EOF

# 备份旧配置
if [ -L /etc/nginx/sites-enabled/news-app ]; then
    mv /etc/nginx/sites-enabled/news-app /etc/nginx/sites-enabled/news-app.bak
fi

# 启用新配置
ln -sf /etc/nginx/sites-available/news-app-full /etc/nginx/sites-enabled/news-app

# 测试配置
nginx -t

if [ $? -eq 0 ]; then
    echo "Nginx 配置测试通过"
    # 重载 Nginx
    nginx -s reload
    echo "Nginx 重载成功"
else
    echo "Nginx 配置测试失败，恢复旧配置"
    rm -f /etc/nginx/sites-enabled/news-app
    if [ -f /etc/nginx/sites-enabled/news-app.bak ]; then
        mv /etc/nginx/sites-enabled/news-app.bak /etc/nginx/sites-enabled/news-app
    fi
    exit 1
fi

ENDNGINX

if [ $? -eq 0 ]; then
    log_success "Nginx 配置更新完成"
else
    log_error "Nginx 配置更新失败"
    exit 1
fi

# ============================================================
# 5. 验证部署
# ============================================================
print_section "5. 验证部署"

log_info "验证前端部署..."

# 测试首页
FRONTEND_TEST_URL="http://${VPS_HOST}/"
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_TEST_URL" || echo "000")

# 测试 API
API_TEST_URL="http://${VPS_HOST}/api/auth/current"
API_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$API_TEST_URL" || echo "000")

if [ "$FRONTEND_STATUS" = "200" ]; then
    log_success "前端页面可访问 (HTTP $FRONTEND_STATUS)"
else
    log_warning "前端页面访问异常 (HTTP $FRONTEND_STATUS)"
fi

if [ "$API_STATUS" = "200" ] || [ "$API_STATUS" = "401" ]; then
    log_success "API 可访问 (HTTP $API_STATUS)"
else
    log_warning "API 访问异常 (HTTP $API_STATUS)"
fi

# ============================================================
# 6. 显示部署信息
# ============================================================
print_section "部署完成"

log_success "✅ 前端部署成功!"
echo ""
log_info "访问地址:"
echo "  - 前端首页: http://${VPS_HOST}/"
echo "  - API 接口: http://${VPS_HOST}/api/*"
echo ""
log_info "前端文件位置:"
ssh ${VPS_USER}@${VPS_HOST} "ls -lh ${VPS_FRONTEND_DIR}/"
echo ""
log_info "Nginx 配置:"
ssh ${VPS_USER}@${VPS_HOST} "nginx -T | grep -A 50 'server {' | head -60"
echo ""

echo "=================================================================="
echo "  🎉 前端部署成功!"
echo "=================================================================="
echo ""
