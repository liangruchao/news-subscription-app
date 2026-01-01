#!/bin/bash

# ============================================================
# VPS 自动部署脚本
# 用途: 将应用自动部署到 VPS
# 使用: ./scripts/deploy-vps.sh [环境]
# 环境: staging (测试) | production (生产)
# ============================================================

set -e

# ============================================================
# 配置
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# VPS 配置
VPS_HOST="47.103.204.114"
VPS_USER="root"
VPS_APP_DIR="/var/www/news-app"
VPS_SERVICE_NAME="newsapp"

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
# 参数检查
# ============================================================
ENVIRONMENT=${1:-production}
case $ENVIRONMENT in
    staging|production)
        ;;
    *)
        log_error "无效的环境参数: $ENVIRONMENT"
        echo "使用方法: $0 [staging|production]"
        exit 1
        ;;
esac

log_info "开始 VPS 部署 - 环境: $ENVIRONMENT"

# ============================================================
# 1. 本地构建
# ============================================================
print_section "1. 本地构建"

log_info "构建后端应用..."
cd "${PROJECT_ROOT}/backend"
mvn clean package -DskipTests -Dspring.profiles.active=${ENVIRONMENT}

JAR_FILE=$(find target -name "*.jar" -not -name "*sources.jar" | head -1)
if [ ! -f "$JAR_FILE" ]; then
    log_error "找不到构建的 JAR 文件"
    exit 1
fi

log_success "JAR 文件构建成功: $JAR_FILE"

# ============================================================
# 2. 备份 VPS 上的现有应用
# ============================================================
print_section "2. 备份现有应用"

log_info "连接到 VPS 并备份现有应用..."
ssh ${VPS_USER}@${VPS_HOST} << 'ENDSSH'
if [ -f /var/www/news-app/news-subscription-backend-*.jar ]; then
    BACKUP_DIR="/var/backups/news-app"
    mkdir -p "$BACKUP_DIR"
    cp /var/www/news-app/news-subscription-backend-*.jar "$BACKUP_DIR/news-app-$(date +%Y%m%d_%H%M%S).jar"
    echo "备份完成"
else
    echo "没有现有应用需要备份"
fi
ENDSSH

log_success "备份完成"

# ============================================================
# 3. 上传 JAR 文件
# ============================================================
print_section "3. 上传 JAR 文件"

log_info "上传 JAR 文件到 VPS..."
scp "${JAR_FILE}" ${VPS_USER}@${VPS_HOST}:/tmp/news-app.jar

log_success "JAR 文件上传完成"

# ============================================================
# 4. 上传配置文件
# ============================================================
print_section "4. 上传配置文件"

log_info "上传 application-${ENVIRONMENT}.properties..."
if [ -f "src/main/resources/application-${ENVIRONMENT}.properties" ]; then
    scp "src/main/resources/application-${ENVIRONMENT}.properties" \
        ${VPS_USER}@${VPS_HOST}:/tmp/application.properties
else
    log_warning "未找到 application-${ENVIRONMENT}.properties，使用默认配置"
fi

log_success "配置文件上传完成"

# ============================================================
# 5. 更新 VPS 上的应用
# ============================================================
print_section "5. 更新 VPS 应用"

ssh ${VPS_USER}@${VPS_HOST} << 'ENDSSH'
# 停止服务
echo "停止服务..."
systemctl stop newsapp

# 备份当前 JAR
if [ -f /var/www/news-app/news-subscription-backend-*.jar ]; then
    mv /var/www/news-app/news-subscription-backend-*.jar /var/www/news-app/news-app.backup.jar
fi

# 复制新 JAR
echo "复制新 JAR 文件..."
cp /tmp/news-app.jar /var/www/news-app/news-subscription-backend-1.0.0.jar

# 复制配置文件
if [ -f /tmp/application.properties ]; then
    cp /tmp/application.properties /var/www/news-app/application.properties
fi

# 设置权限
chown -R newsapp:newsapp /var/www/news-app
chmod 644 /var/www/news-app/news-subscription-backend-1.0.0.jar

# 重新加载 systemd
echo "重新加载 systemd..."
systemctl daemon-reload
ENDSSH

log_success "应用更新完成"

# ============================================================
# 6. 重启服务
# ============================================================
print_section "6. 重启服务"

log_info "启动服务..."
ssh ${VPS_USER}@${VPS_HOST} << 'ENDSSH'
# 启动服务
systemctl start newsapp

# 等待服务启动
echo "等待服务启动..."
sleep 10

# 检查服务状态
if systemctl is-active --quiet newsapp; then
    echo "✅ 服务启动成功"
    systemctl status newsapp --no-pager
else
    echo "❌ 服务启动失败"
    journalctl -u newsapp -n 50 --no-pager
    exit 1
fi
ENDSSH

log_success "服务重启完成"

# ============================================================
# 7. 健康检查
# ============================================================
print_section "7. 健康检查"

log_info "执行健康检查..."
sleep 5

# 测试 API 连通性
HEALTH_CHECK_URL="http://${VPS_HOST}/api/auth/current"
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_CHECK_URL" || echo "000")

if [ "$HTTP_STATUS" = "200" ] || [ "$HTTP_STATUS" = "401" ]; then
    log_success "健康检查通过 (HTTP $HTTP_STATUS)"
else
    log_error "健康检查失败 (HTTP $HTTP_STATUS)"
    log_info "URL: $HEALTH_CHECK_URL"
    exit 1
fi

# ============================================================
# 8. 清理临时文件
# ============================================================
print_section "8. 清理临时文件"

log_info "清理临时文件..."
ssh ${VPS_USER}@${VPS_HOST} << 'ENDSSH'
rm -f /tmp/news-app.jar /tmp/application.properties
ENDSSH

log_success "临时文件清理完成"

# ============================================================
# 完成
# ============================================================
print_section "部署完成"

log_success "✅ VPS 部署成功!"
log_info "环境: $ENVIRONMENT"
log_info "VPS: $VPS_HOST"
log_info "部署时间: $(date)"
log_info ""
log_info "查看日志: ssh ${VPS_USER}@${VPS_HOST} 'journalctl -u newsapp -f'"
log_info "查看状态: ssh ${VPS_USER}@${VPS_HOST} 'systemctl status newsapp'"

echo ""
echo "=================================================================="
echo "  🎉 VPS 部署成功!"
echo "=================================================================="
echo ""
