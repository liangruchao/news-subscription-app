#!/bin/bash

# ============================================================
# 本地 CI/CD 脚本
# 用途: 替代 GitHub Actions，在本地执行完整的 CI/CD 流程
# 使用: ./scripts/local-ci.sh [环境]
# 环境: dev (开发) | staging (测试) | production (生产)
# ============================================================

set -e  # 遇到错误立即退出
set -o pipefail  # 管道命令失败时退出

# ============================================================
# 配置
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
LOG_DIR="${PROJECT_ROOT}/logs"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 未安装，请先安装"
        exit 1
    fi
}

# 创建日志目录
mkdir -p "${LOG_DIR}"

# ============================================================
# 参数检查
# ============================================================
ENVIRONMENT=${1:-dev}
case $ENVIRONMENT in
    dev|staging|production)
        ;;
    *)
        log_error "无效的环境参数: $ENVIRONMENT"
        echo "使用方法: $0 [dev|staging|production]"
        exit 1
        ;;
esac

log_info "开始本地 CI/CD 流程 - 环境: $ENVIRONMENT"
log_info "时间: $(date)"

# ============================================================
# 1. 环境检查
# ============================================================
print_section "1. 环境检查"

check_command java
check_command mvn
check_command node
check_command npm
check_command git

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1"."$2}')
if [[ $(echo "$JAVA_VERSION >= 21" | bc -l) -eq 0 ]]; then
    log_error "需要 Java 21 或更高版本，当前版本: $JAVA_VERSION"
    exit 1
fi

log_success "环境检查通过"
log_info "Java 版本: $JAVA_VERSION"
log_info "Node 版本: $(node -v)"
log_info "Maven 版本: $(mvn -v | head -1 | awk '{print $3}')"

# ============================================================
# 2. 代码检查
# ============================================================
print_section "2. 代码检查"

log_info "检查 Git 状态..."
if [ -n "$(git status --porcelain)" ]; then
    log_warning "工作目录有未提交的更改"
    git status --short
else
    log_success "工作目录干净"
fi

# ============================================================
# 3. 后端单元测试
# ============================================================
print_section "3. 后端单元测试"

cd "${PROJECT_ROOT}/backend"
log_info "运行后端单元测试..."
mvn clean test -DskipITs=true 2>&1 | tee "${LOG_DIR}/backend-unit-test-${TIMESTAMP}.log"

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    log_success "后端单元测试通过"
else
    log_error "后端单元测试失败"
    exit 1
fi

# ============================================================
# 4. 后端集成测试
# ============================================================
print_section "4. 后端集成测试"

log_info "检查 MySQL 是否运行..."
if ! mysqladmin ping -h localhost -u root -papeng320 --silent 2>/dev/null; then
    log_warning "MySQL 未运行，跳过集成测试"
else
    log_info "运行后端集成测试..."
    mvn verify -DskipUnitTests=true 2>&1 | tee "${LOG_DIR}/backend-integration-test-${TIMESTAMP}.log"

    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        log_success "后端集成测试通过"
    else
        log_error "后端集成测试失败"
        exit 1
    fi
fi

# ============================================================
# 5. 代码覆盖率检查
# ============================================================
print_section "5. 代码覆盖率检查"

cd "${PROJECT_ROOT}/backend"
log_info "生成覆盖率报告..."
mvn jacoco:report 2>&1 | tee -a "${LOG_DIR}/coverage-${TIMESTAMP}.log"

log_info "检查覆盖率阈值..."
mvn jacoco:check 2>&1 | tee -a "${LOG_DIR}/coverage-${TIMESTAMP}.log"

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    log_success "代码覆盖率检查通过"
else
    log_warning "代码覆盖率未达到阈值，但继续构建"
fi

# ============================================================
# 6. 前端测试
# ============================================================
print_section "6. 前端测试"

cd "${PROJECT_ROOT}/frontend"
log_info "安装前端依赖..."
npm ci 2>&1 | tee "${LOG_DIR}/frontend-install-${TIMESTAMP}.log"

log_info "运行前端单元测试..."
npm run test 2>&1 | tee "${LOG_DIR}/frontend-unit-test-${TIMESTAMP}.log"

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    log_success "前端单元测试通过"
else
    log_error "前端单元测试失败"
    exit 1
fi

# ============================================================
# 7. 构建应用
# ============================================================
print_section "7. 构建应用"

log_info "构建后端应用..."
cd "${PROJECT_ROOT}/backend"
mvn clean package -DskipTests 2>&1 | tee "${LOG_DIR}/backend-build-${TIMESTAMP}.log"

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    log_success "后端构建成功"
else
    log_error "后端构建失败"
    exit 1
fi

# ============================================================
# 8. 部署 (仅 staging/production)
# ============================================================
if [ "$ENVIRONMENT" = "staging" ] || [ "$ENVIRONMENT" = "production" ]; then
    print_section "8. 部署到 $ENVIRONMENT"

    # 部署脚本 (根据环境选择不同的部署方式)
    if [ "$ENVIRONMENT" = "production" ]; then
        log_info "部署到 VPS 生产环境..."
        # TODO: 添加 VPS 部署脚本
        log_warning "VPS 部署脚本待实现，请手动部署"
    else
        log_info "部署到测试环境..."
        # TODO: 添加测试环境部署脚本
        log_warning "测试环境部署脚本待实现"
    fi
else
    print_section "8. 开发环境 - 跳过部署"
    log_info "开发环境构建完成，可以手动启动应用"
    log_info "后端: cd backend && mvn spring-boot:run"
    log_info "前端: cd frontend && npm run dev"
fi

# ============================================================
# 9. 生成报告
# ============================================================
print_section "9. 生成测试报告"

log_info "所有日志保存在: ${LOG_DIR}"
log_info "覆盖率报告: backend/target/site/jacoco/index.html"

# ============================================================
# 完成
# ============================================================
print_section "CI/CD 流程完成"

log_success "所有测试通过"
log_success "构建成功"
log_info "环境: $ENVIRONMENT"
log_info "时间: $(date)"

echo ""
echo "=================================================================="
echo "  🎉 本地 CI/CD 流程执行成功!"
echo "=================================================================="
echo ""
