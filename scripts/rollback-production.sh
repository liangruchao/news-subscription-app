#!/bin/bash

# ============================================================
# Production 环境回滚脚本
# 用途: 回滚到指定的镜像版本
# 使用: ./scripts/rollback-production.sh [image_tag]
# ============================================================

set -e

# ============================================================
# 配置
# ============================================================
VPS_HOST="${PRODUCTION_VPS_HOST:-139.224.189.183}"
VPS_USER="${PRODUCTION_VPS_USER:-root}"
VPS_PROJECT_DIR="${PRODUCTION_PROJECT_DIR:-/var/www/news-app}"

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
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================================
# 欢迎信息
# ============================================================
echo ""
echo "=================================================================="
echo "  Production 环境回滚工具"
echo "=================================================================="
echo ""
echo "  VPS: $VPS_HOST"
echo "  项目目录: $VPS_PROJECT_DIR"
echo ""
echo "  ⚠️  警告: 此操作将回滚生产环境!"
echo ""
echo "=================================================================="
echo ""

# ============================================================
# 获取可用的镜像
# ============================================================
log_info "获取可用的镜像版本..."

ssh ${VPS_USER}@${VPS_HOST} << 'EOF'
echo "可用的镜像版本:"
docker images | grep news-app | grep production | awk '{print "  " $1 ":" $2 " (" $3 ")"}'
EOF

echo ""

# ============================================================
# 获取要回滚的版本
# ============================================================
if [ -n "$1" ]; then
    IMAGE_TAG="$1"
    log_info "使用指定的镜像标签: $IMAGE_TAG"
else
    read -p "输入要回滚到的镜像标签 (如: production-abc1234): " IMAGE_TAG
fi

if [ -z "$IMAGE_TAG" ]; then
    log_error "镜像标签不能为空"
    exit 1
fi

# ============================================================
# 确认回滚
# ============================================================
echo ""
log_warning "即将回滚到: $IMAGE_TAG"
read -p "确认回滚? (输入 yes 继续): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    log_warning "回滚已取消"
    exit 0
fi

echo ""
log_info "开始回滚流程..."

# ============================================================
# 执行回滚
# ============================================================
ssh ${VPS_USER}@${VPS_HOST} << ROLLBACK_EOF
set -e

PROJECT_DIR="/var/www/news-app"
cd ${PROJECT_DIR}

echo ""
echo "=================================================================="
echo "  执行回滚"
echo "=================================================================="

# ============================================================
# 确定当前环境
# ============================================================
if docker ps | grep -q "news-production-backend-blue"; then
    CURRENT_COLOR="blue"
    NEW_COLOR="green"
    NEW_PORT=8082
elif docker ps | grep -q "news-production-backend-green"; then
    CURRENT_COLOR="green"
    NEW_COLOR="blue"
    NEW_PORT=8081
else
    echo "错误: 无法确定当前运行的环境"
    exit 1
fi

echo "当前环境: $CURRENT_COLOR"
echo "回滚环境: $NEW_COLOR (端口 $NEW_PORT)"
echo "回滚镜像: $IMAGE_TAG"

# ============================================================
# 检查镜像是否存在
# ============================================================
echo ""
echo "检查镜像是否存在..."
if ! docker images | grep -q "news-app.*${IMAGE_TAG}"; then
    echo "错误: 镜像 news-app:${IMAGE_TAG} 不存在"
    echo ""
    echo "可用的镜像:"
    docker images | grep news-app | grep production
    exit 1
fi

echo "✓ 镜像存在"

# ============================================================
# 停止并删除新环境
# ============================================================
echo ""
echo "停止 $NEW_COLOR 环境..."
export COMPOSE_PROJECT_NAME=news-production-${NEW_COLOR}
docker-compose --env-file ../.env down 2>/dev/null || true

# ============================================================
# 启动回滚版本
# ============================================================
echo ""
echo "启动回滚版本..."

# 更新 .env 中的 GIT_COMMIT
OLD_GIT_COMMIT=$(grep GIT_COMMIT .env | cut -d'=' -f2)
echo "旧版本: $OLD_GIT_COMMIT"
echo "新版本: ${IMAGE_TAG##*-}"

# 启动回滚环境
export BACKEND_PORT=$NEW_PORT
export COMPOSE_PROJECT_NAME=news-production-${NEW_COLOR}
export GIT_COMMIT="${IMAGE_TAG##*-}"

# 使用指定的镜像启动
docker-compose --env-file ../.env up -d

# 修改容器使用的镜像
docker tag news-app:${IMAGE_TAG} news-app:${NEW_COLOR}-current
docker update --restart=no news-production-backend-${NEW_COLOR} 2>/dev/null || true

# ============================================================
# 等待健康检查
# ============================================================
echo ""
echo "等待回滚环境启动..."
for i in {1..60}; do
    if curl -f http://localhost:${NEW_PORT}/api/auth/current >/dev/null 2>&1; then
        echo ""
        echo "✓ 回滚环境健康检查通过! ($i 秒)"
        break
    fi
    if [ $i -eq 60 ]; then
        echo ""
        echo "✗ 回滚环境启动超时"
        docker-compose --env-file ../.env down
        exit 1
    fi
    echo -n "."
    sleep 2
done

# ============================================================
# 切换 Nginx
# ============================================================
echo ""
echo "切换 Nginx 到回滚环境..."

cat > /etc/nginx/conf.d/news-app.conf << 'NGINX_CONF'
server {
    listen 80;
    server_name _;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files \$uri \$uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://backend-NEW_PLACEHOLDER:NEW_PORT_PLACEHOLDER;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
NGINX_CONF

sed -i "s/backend-NEW_PLACEHOLDER/backend-${NEW_COLOR}/" /etc/nginx/conf.d/news-app.conf
sed -i "s/NEW_PORT_PLACEHOLDER/${NEW_PORT}/" /etc/nginx/conf.d/news-app.conf

nginx -t
nginx -s reload

echo "✓ Nginx 已切换到 $NEW_COLOR 环境"

# ============================================================
# 等待并验证
# ============================================================
echo ""
echo "等待 10 秒确认..."
sleep 10

if curl -f http://localhost/health >/dev/null 2>&1; then
    echo "✓ 公网访问正常"
else
    echo "✗ 公网访问失败"
    exit 1
fi

# ============================================================
# 记录回滚
# ============================================================
echo ""
echo "记录回滚信息..."
cat >> ${PROJECT_DIR}/deployments.log << ROLLBACK_LOG
$(date '+%Y-%m-%d %H:%M:%S') - ROLLED BACK to $IMAGE_TAG (environment: $NEW_COLOR, from: $CURRENT_COLOR)
ROLLBACK_LOG

echo ""
echo "=================================================================="
echo "  回滚完成!"
echo "=================================================================="
echo "  回滚环境:     $NEW_COLOR"
echo "  回滚镜像:     $IMAGE_TAG"
echo "  回滚时间:     $(date)"
echo "=================================================================="
echo ""

ROLLBACK_EOF

if [ $? -eq 0 ]; then
    log_success "回滚成功!"
else
    log_error "回滚失败!"
    exit 1
fi

# ============================================================
# 健康检查
# ============================================================
echo ""
log_info "运行健康检查..."

if [ -f "$(dirname "$0")/health-check.sh" ]; then
    $(dirname "$0")/health-check.sh ${VPS_HOST} production
else
    log_warning "健康检查脚本不存在"
    log_info "手动检查: curl http://${VPS_HOST}/health"
fi

# ============================================================
# 完成
# ============================================================
echo ""
echo "=================================================================="
echo "  回滚完成"
echo "=================================================================="
echo ""
echo "  访问地址: http://${VPS_HOST}/"
echo "  查看日志: ssh ${VPS_USER}@${VPS_HOST} 'cd ${VPS_PROJECT_DIR} && docker-compose logs -f'"
echo ""
echo "=================================================================="
echo ""
