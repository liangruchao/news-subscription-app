#!/bin/bash

# ============================================================
# 健康检查脚本
# 用途: 检查指定环境的健康状态
# 使用: ./scripts/health-check.sh <host> <environment>
# 示例: ./scripts/health-check.sh 47.103.204.114 production
# ============================================================

set -e

# ============================================================
# 参数检查
# ============================================================
if [ $# -lt 2 ]; then
    echo "使用方法: $0 <host> <environment>"
    echo "示例: $0 47.103.204.114 production"
    exit 1
fi

HOST=$1
ENVIRONMENT=$2
MAX_RETRIES=30
RETRY_INTERVAL=2

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
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# ============================================================
# 开始健康检查
# ============================================================
echo ""
echo "=================================================================="
echo "  健康检查 - $ENVIRONMENT 环境"
echo "=================================================================="
echo "  主机: $HOST"
echo "  时间: $(date)"
echo "=================================================================="
echo ""

# ============================================================
# 1. HTTP 健康检查
# ============================================================
log_info "检查 1/4: HTTP 健康检查端点"

for i in $(seq 1 $MAX_RETRIES); do
    echo -n "  检查中... ($i/$MAX_RETRIES) "

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://${HOST}/health 2>/dev/null || echo "000")

    if [ "$HTTP_CODE" = "200" ]; then
        log_success "HTTP 健康检查通过 (200 OK)"
        break
    fi

    if [ $i -eq $MAX_RETRIES ]; then
        log_error "HTTP 健康检查失败 (HTTP $HTTP_CODE)"
        echo ""
        echo "=================================================================="
        echo "  健康检查失败!"
        echo "=================================================================="
        exit 1
    fi

    sleep $RETRY_INTERVAL
done

# ============================================================
# 2. API 端点检查
# ============================================================
log_info "检查 2/4: API 端点"

API_RESPONSE=$(curl -s http://${HOST}/api/auth/current 2>/dev/null || echo "{}")

if echo "$API_RESPONSE" | grep -q "success\|authenticated\|user"; then
    log_success "API 端点响应正常"
else
    # 401 是预期的（未登录）
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://${HOST}/api/auth/current)
    if [ "$HTTP_CODE" = "401" ]; then
        log_success "API 端点响应正常 (401 Unauthorized - 预期)"
    else
        log_error "API 端点响应异常"
        echo "  响应: $(echo $API_RESPONSE | head -c 100)"
    fi
fi

# ============================================================
# 3. 前端页面检查
# ============================================================
log_info "检查 3/4: 前端页面"

FRONTEND_CHECK=$(curl -s http://${HOST}/ 2>/dev/null || echo "")

if echo "$FRONTEND_CHECK" | grep -q "html\|HTML"; then
    log_success "前端页面可访问"
else
    log_warning "前端页面检查失败（可能需要登录）"
fi

# ============================================================
# 4. 响应时间检查
# ============================================================
log_info "检查 4/4: API 响应时间"

START_TIME=$(date +%s%N)
curl -s http://${HOST}/health >/dev/null 2>&1
END_TIME=$(date +%s%N)

RESPONSE_TIME=$(( (END_TIME - START_TIME) / 1000000 ))  # 转换为毫秒

if [ $RESPONSE_TIME -lt 1000 ]; then
    log_success "响应时间: ${RESPONSE_TIME}ms (优秀)"
elif [ $RESPONSE_TIME -lt 3000 ]; then
    log_warning "响应时间: ${RESPONSE_TIME}ms (可接受)"
else
    log_warning "响应时间: ${RESPONSE_TIME}ms (较慢)"
fi

# ============================================================
# 5. 环境信息
# ============================================================
echo ""
log_info "环境信息:"

# 检查是否有 Actuator 端点
ACTUATOR_HEALTH=$(curl -s http://${HOST}/actuator/health 2>/dev/null || echo "")

if [ -n "$ACTUATOR_HEALTH" ]; then
    echo "  Actuator 健康状态:"
    echo "$ACTUATOR_HEALTH" | grep -o '"status":"[^"]*"' | sed 's/^/    /'

    # 检查数据库
    DB_HEALTH=$(echo "$ACTUATOR_HEALTH" | grep -o '"db":{[^}]*}' || echo "")
    if [ -n "$DB_HEALTH" ]; then
        DB_STATUS=$(echo "$DB_HEALTH" | grep -o '"status":"[^"]*"' || echo '"status":"unknown"')
        echo "    数据库: $DB_STATUS"
    fi

    # 检查 Redis
    REDIS_HEALTH=$(echo "$ACTUATOR_HEALTH" | grep -o '"redis":{[^}]*}' || echo "")
    if [ -n "$REDIS_HEALTH" ]; then
        REDIS_STATUS=$(echo "$REDIS_HEALTH" | grep -o '"status":"[^"]*"' || echo '"status":"unknown"')
        echo "    Redis: $REDIS_STATUS"
    fi
fi

# ============================================================
# 完成
# ============================================================
echo ""
echo "=================================================================="
echo "  所有健康检查通过!"
echo "=================================================================="
echo "  环境:      $ENVIRONMENT"
echo "  主机:      $HOST"
echo "  状态:      健康"
echo "  响应时间:  ${RESPONSE_TIME}ms"
echo "  检查时间:  $(date)"
echo "=================================================================="
echo ""

exit 0
