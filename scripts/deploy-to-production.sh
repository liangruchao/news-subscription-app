#!/bin/bash

# ============================================================
# 部署到 Production 环境脚本（蓝绿部署）
# 用途: 使用蓝绿部署策略安全地部署到生产环境
# 使用: ./scripts/deploy-to-production.sh
# ============================================================

set -e  # 遇到错误立即退出
set -o pipefail

# ============================================================
# 配置
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENVIRONMENT="production"

# VPS 配置
VPS_HOST="${PRODUCTION_VPS_HOST:-139.224.189.183}"
VPS_USER="${PRODUCTION_VPS_USER:-root}"
VPS_PROJECT_DIR="${PRODUCTION_PROJECT_DIR:-/var/www/news-app}"

# 蓝绿端口配置
BLUE_PORT=8081
GREEN_PORT=8082
CURRENT_COLOR="unknown"
NEW_COLOR="unknown"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# ============================================================
# 函数定义
# ============================================================
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

print_section() {
    echo ""
    echo "=================================================================="
    echo "  $1"
    echo "=================================================================="
    echo ""
}

# ============================================================
# 确认部署
# ============================================================
print_section "生产环境部署确认"

echo ""
echo "⚠️  警告: 即将部署到生产环境!"
echo ""
echo "  VPS:         $VPS_HOST"
echo "  项目目录:    $VPS_PROJECT_DIR"
echo ""
read -p "确认部署到生产环境? (输入 yes 继续): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    log_warning "部署已取消"
    exit 0
fi

echo ""
log_info "开始生产环境部署流程..."

# ============================================================
# 检查前置条件
# ============================================================
print_section "检查前置条件"

# 检查 Docker
if ! command -v docker &> /dev/null; then
    log_error "Docker 未安装"
    exit 1
fi

# 检查 SSH
if ! command -v ssh &> /dev/null; then
    log_error "SSH 客户端未安装"
    exit 1
fi

log_success "前置条件检查通过"

# ============================================================
# 获取 Git 提交信息
# ============================================================
print_section "获取 Git 信息"

GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
BUILD_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

log_info "Git 提交: $GIT_COMMIT"
log_info "Git 分支: $GIT_BRANCH"
log_info "构建时间: $BUILD_TIME"

# 检查分支是否为 main
if [ "$GIT_BRANCH" != "main" ]; then
    log_warning "当前不在 main 分支，确认继续?"
    read -p "继续部署? (yes/no): " BRANCH_CONFIRM
    if [ "$BRANCH_CONFIRM" != "yes" ]; then
        log_info "部署已取消"
        exit 0
    fi
fi

# ============================================================
# 构建 Docker 镜像
# ============================================================
print_section "构建 Docker 镜像"

IMAGE_TAG="${ENVIRONMENT}-${GIT_COMMIT}"
IMAGE_NAME="news-app"

log_info "镜像名称: $IMAGE_NAME:$IMAGE_TAG"

cd "$PROJECT_ROOT"

# 构建镜像
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} \
    --build-arg GIT_COMMIT=${GIT_COMMIT} \
    --build-arg BUILD_TIME=${BUILD_TIME} \
    --build-arg ENVIRONMENT=${ENVIRONMENT} \
    -f Dockerfile .

if [ $? -eq 0 ]; then
    log_success "Docker 镜像构建成功"
else
    log_error "Docker 镜像构建失败"
    exit 1
fi

# 同时打上 latest 标签
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:${ENVIRONMENT}-latest

# ============================================================
# 保存镜像
# ============================================================
print_section "保存 Docker 镜像"

ARCHIVE_FILE="/tmp/${IMAGE_NAME}-${IMAGE_TAG}.tar.gz"

log_info "保存镜像到: $ARCHIVE_FILE"
docker save ${IMAGE_NAME}:${IMAGE_TAG} | gzip > "$ARCHIVE_FILE"

if [ $? -eq 0 ]; then
    log_success "镜像保存成功"
    ls -lh "$ARCHIVE_FILE"
else
    log_error "镜像保存失败"
    exit 1
fi

# ============================================================
# 上传到 VPS
# ============================================================
print_section "上传到 VPS"

# 在 VPS 上创建目录
log_info "在 VPS 上创建目录..."
ssh ${VPS_USER}@${VPS_HOST} "mkdir -p ${VPS_PROJECT_DIR}/{logs/{backend,nginx},backups,nginx/conf.d,nginx/ssl}"

# 上传镜像
log_info "上传 Docker 镜像..."
scp "$ARCHIVE_FILE" ${VPS_USER}@${VPS_HOST}:${VPS_PROJECT_DIR}/

# 上传配置文件
log_info "上传配置文件..."
scp deploy/${ENVIRONMENT}/docker-compose.yml ${VPS_USER}@${VPS_HOST}:${VPS_PROJECT_DIR}/
scp deploy/${ENVIRONMENT}/.env ${VPS_USER}@${VPS_HOST}:${VPS_PROJECT_DIR}/
scp deploy/${ENVIRONMENT}/nginx/conf.d/default.conf ${VPS_USER}@${VPS_HOST}:${VPS_PROJECT_DIR}/nginx/conf.d/

log_success "文件上传完成"

# ============================================================
# 在 VPS 上执行蓝绿部署
# ============================================================
print_section "蓝绿部署"

ssh ${VPS_USER}@${VPS_HOST} << 'BLUE_GREEN_EOF'
set -e

PROJECT_DIR="/var/www/news-app"
cd ${PROJECT_DIR}

# ============================================================
# 确定当前环境和目标环境
# ============================================================
echo "检查当前运行环境..."

if docker ps | grep -q "news-production-backend-blue"; then
    CURRENT_COLOR="blue"
    NEW_COLOR="green"
    NEW_PORT=8082
    CURRENT_PORT=8081
elif docker ps | grep -q "news-production-backend-green"; then
    CURRENT_COLOR="green"
    NEW_COLOR="blue"
    NEW_PORT=8081
    CURRENT_PORT=8082
else
    # 没有运行的环境，默认启动 blue
    CURRENT_COLOR="none"
    NEW_COLOR="blue"
    NEW_PORT=8081
    CURRENT_PORT=8082
fi

echo "当前环境: $CURRENT_COLOR (端口 $CURRENT_PORT)"
echo "新环境:   $NEW_COLOR (端口 $NEW_PORT)"

# ============================================================
# 加载新镜像
# ============================================================
IMAGE_ARCHIVE=$(ls -t ${PROJECT_DIR}/news-app-production-*.tar.gz 2>/dev/null | head -1)
if [ -z "$IMAGE_ARCHIVE" ]; then
    echo "错误: 找不到镜像文件"
    exit 1
fi

echo "加载新镜像: $IMAGE_ARCHIVE"
docker load < "$IMAGE_ARCHIVE"

# 获取镜像标签
IMAGE_TAG=$(basename "$IMAGE_ARCHIVE" | sed 's/news-app-production-//' | sed 's/.tar.gz//')
echo "镜像标签: $IMAGE_TAG"

# 更新 .env 中的 GIT_COMMIT
sed -i "s/GIT_COMMIT=.*/GIT_COMMIT=${IMAGE_TAG}/" .env

# ============================================================
# 启动新环境
# ============================================================
echo "启动新环境 ($NEW_COLOR)..."

# 设置端口
export BACKEND_PORT=$NEW_PORT
export COMPOSE_PROJECT_NAME=news-production-${NEW_COLOR}

# 启动新环境
docker-compose --env-file .env up -d

# 等待新环境启动
echo "等待新环境启动..."
for i in {1..60}; do
    if curl -f http://localhost:${NEW_PORT}/api/auth/current >/dev/null 2>&1; then
        echo ""
        echo "✓ 新环境健康检查通过! ($i 秒)"
        break
    fi
    if [ $i -eq 60 ]; then
        echo ""
        echo "✗ 新环境启动超时"
        docker-compose --env-file .env down
        exit 1
    fi
    echo -n "."
    sleep 2
done

# ============================================================
# 运行冒烟测试
# ============================================================
echo ""
echo "运行冒烟测试..."

# 测试 API 端点
SMOKE_TEST_RESULT=0

curl -f http://localhost:${NEW_PORT}/api/auth/current >/dev/null 2>&1 || SMOKE_TEST_RESULT=1
if [ $SMOKE_TEST_RESULT -eq 0 ]; then
    echo "✓ API 健康检查通过"
else
    echo "✗ API 健康检查失败"
fi

# 检查数据库连接
DB_HEALTH=$(curl -s http://localhost:${NEW_PORT}/actuator/health 2>/dev/null || echo "{}")
if echo "$DB_HEALTH" | grep -q '"status":"UP"'; then
    echo "✓ 数据库连接正常"
else
    echo "⚠ 数据库健康检查端点不可用 (可能已禁用)"
fi

if [ $SMOKE_TEST_RESULT -ne 0 ]; then
    echo ""
    echo "冒烟测试失败，停止部署..."
    docker-compose --env-file .env down
    exit 1
fi

# ============================================================
# 切换 Nginx 到新环境
# ============================================================
echo ""
echo "切换 Nginx 到新环境..."

# 更新 Nginx 配置
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

# 替换占位符
sed -i "s/backend-NEW_PLACEHOLDER/backend-${NEW_COLOR}/" /etc/nginx/conf.d/news-app.conf
sed -i "s/NEW_PORT_PLACEHOLDER/${NEW_PORT}/" /etc/nginx/conf.d/news-app.conf

# 测试 Nginx 配置
nginx -t

# 重新加载 Nginx
nginx -s reload

echo "✓ Nginx 已切换到 $NEW_COLOR 环境"

# ============================================================
# 等待并验证新环境
# ============================================================
echo ""
echo "等待 10 秒确认新环境稳定..."
sleep 10

# 检查公网访问
if curl -f http://localhost/health >/dev/null 2>&1; then
    echo "✓ 公网访问正常"
else
    echo "✗ 公网访问失败，回滚!"
    # 回滚 Nginx
    sed -i "s/backend-${NEW_COLOR}/backend-${CURRENT_COLOR}/" /etc/nginx/conf.d/news-app.conf
    sed -i "s/${NEW_PORT}/${CURRENT_PORT}/" /etc/nginx/conf.d/news-app.conf
    nginx -s reload
    # 停止新环境
    docker-compose --env-file .env down
    exit 1
fi

# ============================================================
# 停止旧环境
# ============================================================
if [ "$CURRENT_COLOR" != "none" ]; then
    echo ""
    echo "停止旧环境 ($CURRENT_COLOR)..."
    export COMPOSE_PROJECT_NAME=news-production-${CURRENT_COLOR}
    docker-compose --env-file ../.env down
    echo "✓ 旧环境已停止"
fi

# ============================================================
# 清理旧镜像
# ============================================================
echo ""
echo "清理旧镜像..."
docker image prune -f

# 删除上传的归档文件
rm -f ${PROJECT_DIR}/news-app-production-*.tar.gz

# ============================================================
# 记录部署信息
# ============================================================
echo ""
echo "记录部署信息..."
cat >> ${PROJECT_DIR}/deployments.log << DEPLOY_LOG
$(date '+%Y-%m-%d %H:%M:%S') - Deployed $IMAGE_TAG to $NEW_COLOR (from $CURRENT_COLOR)
DEPLOY_LOG

echo ""
echo "=================================================================="
echo "  蓝绿部署完成!"
echo "=================================================================="
echo "  新环境:     $NEW_COLOR (端口 $NEW_PORT)"
echo "  镜像标签:   $IMAGE_TAG"
echo "  部署时间:   $(date)"
echo "=================================================================="
echo ""

BLUE_GREEN_EOF

if [ $? -eq 0 ]; then
    log_success "生产环境蓝绿部署成功"
else
    log_error "生产环境蓝绿部署失败"
    exit 1
fi

# ============================================================
# 本地清理
# ============================================================
print_section "本地清理"

log_info "删除临时文件..."
rm -f "$ARCHIVE_FILE"

log_info "清理本地旧镜像..."
docker image prune -f

# ============================================================
# 健康检查
# ============================================================
print_section "生产环境健康检查"

if [ -f "${SCRIPT_DIR}/health-check.sh" ]; then
    log_info "运行健康检查..."
    ${SCRIPT_DIR}/health-check.sh ${VPS_HOST} ${ENVIRONMENT}
else
    log_warning "健康检查脚本不存在"
    log_info "手动检查: curl http://${VPS_HOST}/health"
fi

# ============================================================
# 冒烟测试
# ============================================================
print_section "生产环境冒烟测试"

log_info "运行冒烟测试..."
curl -s http://${VPS_HOST}/health && log_success "健康检查端点正常" || log_error "健康检查端点失败"
curl -s http://${VPS_HOST}/api/auth/current | head -c 100 && echo "" && log_success "API 端点响应正常" || log_error "API 端点无响应"

# ============================================================
# 完成
# ============================================================
print_section "部署完成"

log_success "生产环境部署成功!"
echo ""
echo "=================================================================="
echo "  环境信息"
echo "=================================================================="
echo "  环境:        $ENVIRONMENT"
echo "  VPS:         $VPS_HOST"
echo "  Git 提交:    $GIT_COMMIT"
echo "  镜像标签:    $IMAGE_TAG"
echo "  部署方式:    蓝绿部署"
echo "  部署时间:    $(date)"
echo ""
echo "=================================================================="
echo "  访问地址"
echo "=================================================================="
echo "  前端:        http://${VPS_HOST}/"
echo "  API:         http://${VPS_HOST}/api/"
echo "  健康检查:    http://${VPS_HOST}/health"
echo ""
echo "=================================================================="
echo "  后续操作"
echo "=================================================================="
echo "  1. 访问应用进行功能测试"
echo "  2. 查看日志: ssh ${VPS_USER}@${VPS_HOST} 'cd ${VPS_PROJECT_DIR} && docker-compose logs -f'"
echo "  3. 监控性能: ssh ${VPS_USER}@${VPS_HOST} 'docker stats'"
echo "  4. 运行 E2E 测试: cd frontend && npm run test:e2e:production"
echo ""
echo "=================================================================="
echo "  回滚命令 (如需要)"
echo "=================================================================="
echo "  ./scripts/rollback-production.sh $IMAGE_TAG"
echo ""
