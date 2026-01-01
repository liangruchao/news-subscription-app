#!/bin/bash

# ============================================================
# 部署到 Staging 环境脚本
# 用途: 自动构建、上传并部署应用到 Staging 环境
# 使用: ./scripts/deploy-to-staging.sh
# ============================================================

set -e  # 遇到错误立即退出
set -o pipefail

# ============================================================
# 配置
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ENVIRONMENT="staging"

# VPS 配置
VPS_HOST="${STAGING_VPS_HOST:-47.103.204.114}"
VPS_USER="${STAGING_VPS_USER:-root}"
VPS_PROJECT_DIR="${STAGING_PROJECT_DIR:-/var/www/news-app-staging}"

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
log_info "VPS: $VPS_HOST"
log_info "项目目录: $VPS_PROJECT_DIR"

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
ssh ${VPS_USER}@${VPS_HOST} "mkdir -p ${VPS_PROJECT_DIR}/{logs/{backend,nginx},backups,nginx/conf.d}"

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
# 在 VPS 上部署
# ============================================================
print_section "在 VPS 上部署"

ssh ${VPS_USER}@${VPS_HOST} << EOF
set -e

cd ${VPS_PROJECT_DIR}

# ============================================================
# 加载新镜像
# ============================================================
echo "加载 Docker 镜像..."
docker load < ${VPS_PROJECT_DIR}/$(basename $ARCHIVE_FILE)

# 更新镜像标签
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:${ENVIRONMENT}-current

# 更新 .env 中的 GIT_COMMIT
sed -i "s/GIT_COMMIT=.*/GIT_COMMIT=${GIT_COMMIT}/" .env

# ============================================================
# 停止旧容器
# ============================================================
echo "停止旧容器..."
docker-compose down

# ============================================================
# 启动新容器
# ============================================================
echo "启动新容器..."
docker-compose up -d

# ============================================================
# 等待健康检查
# ============================================================
echo "等待服务启动..."
sleep 30

# ============================================================
# 检查服务状态
# ============================================================
echo "检查服务状态..."
docker-compose ps
docker-compose logs --tail=20 backend

# ============================================================
# 清理
# ============================================================
echo "清理旧镜像..."
docker image prune -f

# 删除上传的归档文件
rm -f ${VPS_PROJECT_DIR}/$(basename $ARCHIVE_FILE)

echo "部署完成!"
EOF

if [ $? -eq 0 ]; then
    log_success "VPS 部署成功"
else
    log_error "VPS 部署失败"
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
print_section "健康检查"

if [ -f "${SCRIPT_DIR}/health-check.sh" ]; then
    log_info "运行健康检查..."
    ${SCRIPT_DIR}/health-check.sh ${VPS_HOST} ${ENVIRONMENT}
else
    log_warning "健康检查脚本不存在，跳过"
    log_info "手动检查: curl http://${VPS_HOST}/health"
fi

# ============================================================
# 完成
# ============================================================
print_section "部署完成"

log_success "Staging 环境部署成功!"
echo ""
echo "=================================================================="
echo "  环境信息"
echo "=================================================================="
echo "  环境:        $ENVIRONMENT"
echo "  VPS:         $VPS_HOST"
echo "  Git 提交:    $GIT_COMMIT"
echo "  镜像标签:    $IMAGE_TAG"
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
echo "  下一步"
echo "=================================================================="
echo "  1. 访问应用进行功能测试"
echo "  2. 查看日志: ssh ${VPS_USER}@${VPS_HOST} 'cd ${VPS_PROJECT_DIR} && docker-compose logs -f'"
echo "  3. 运行 E2E 测试: cd frontend && npm run test:e2e:staging"
echo ""
