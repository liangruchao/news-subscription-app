#!/bin/bash

# ============================================================
# Production 环境自动化部署脚本
# 用途: 自动拉取代码、构建并重启服务
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

DEPLOY_DIR="/var/www/news-app/news-subscription-app"
SERVICE_NAME="news-app"

# ============================================================
print_section "自动化部署开始"

# ============================================================
# 1. 拉取最新代码
# ============================================================
print_section "1. 拉取最新代码"

cd $DEPLOY_DIR
git fetch origin main
git reset --hard origin/main

log_success "代码已更新"

# ============================================================
# 2. 构建项目
# ============================================================
print_section "2. 构建项目"

cd $DEPLOY_DIR/backend
mvn clean package -DskipTests -q

log_success "构建完成"

# ============================================================
# 3. 备份当前版本
# ============================================================
print_section "3. 备份当前版本"

BACKUP_DIR="/var/backups/news-app/$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR

# 备份旧 jar
if [ -f "$DEPLOY_DIR/backend/target/*.jar" ]; then
    cp $DEPLOY_DIR/backend/target/*.jar $BACKUP_DIR/ 2>/dev/null || true
fi

log_success "已备份到: $BACKUP_DIR"

# ============================================================
# 4. 停止服务
# ============================================================
print_section "4. 停止服务"

if systemctl is-active --quiet $SERVICE_NAME; then
    systemctl stop $SERVICE_NAME
    log_success "服务已停止"
else
    log_warning "服务未运行"
fi

# ============================================================
# 5. 更新数据库（如果有迁移脚本）
# ============================================================
print_section "5. 检查数据库更新"

if [ -f "$DEPLOY_DIR/database/migrate.sql" ]; then
    mysql -u newsapp -p'NewsApp@2026DB' news_app < $DEPLOY_DIR/database/migrate.sql
    log_success "数据库已更新"
else
    log_info "无需数据库迁移"
fi

# ============================================================
# 6. 启动服务
# ============================================================
print_section "6. 启动服务"

systemctl start $SERVICE_NAME
sleep 5

# ============================================================
# 7. 健康检查
# ============================================================
print_section "7. 健康检查"

for i in {1..30}; do
    if curl -s http://localhost:8081/api/auth/current > /dev/null 2>&1; then
        log_success "✓ 服务启动成功"
        break
    fi

    if [ $i -eq 30 ]; then
        log_error "✗ 服务启动失败"

        # 回滚
        log_warning "开始回滚..."
        systemctl stop $SERVICE_NAME
        if [ -f "$BACKUP_DIR"/*.jar ]; then
            cp $BACKUP_DIR/*.jar $DEPLOY_DIR/backend/target/
            systemctl start $SERVICE_NAME
            log_success "已回滚到上一个版本"
        fi
        exit 1
    fi

    sleep 2
done

# ============================================================
# 8. 显示状态
# ============================================================
print_section "8. 服务状态"

systemctl status $SERVICE_NAME --no-pager | head -15

# ============================================================
print_section "部署完成"

log_success "Production 环境部署成功！"
echo ""
echo "服务管理:"
echo "  查看状态: systemctl status $SERVICE_NAME"
echo "  查看日志: journalctl -u $SERVICE_NAME -f"
echo "  重启服务: systemctl restart $SERVICE_NAME"
echo "  停止服务: systemctl stop $SERVICE_NAME"
echo ""
echo "日志文件:"
echo "  应用日志: tail -f /var/log/news-app.log"
echo "  错误日志: tail -f /var/log/news-app-error.log"
echo ""
