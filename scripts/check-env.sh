#!/bin/bash

# ============================================================
# VPS 环境检查和配置脚本
# 用途: 检查并安装 CI/CD 所需的所有组件
# 使用: ./scripts/check-env.sh
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
# 系统信息
# ============================================================
print_section "系统信息"

echo "操作系统: $(cat /etc/os-release | grep PRETTY_NAME | cut -d'"' -f 2)"
echo "内核版本: $(uname -r)"
echo "CPU: $(nproc) 核心"
echo "内存: $(free -h | grep Mem | awk '{print $2}')"
echo "磁盘: $(df -h / | tail -1 | awk '{print $4}') 可用"

# ============================================================
# 检查 Docker
# ============================================================
print_section "检查 Docker"

if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
    log_success "Docker 已安装: $DOCKER_VERSION"
else
    log_warning "Docker 未安装"
    read -p "是否安装 Docker? (yes/no): " INSTALL_DOCKER
    if [ "$INSTALL_DOCKER" = "yes" ]; then
        log_info "安装 Docker..."
        curl -fsSL https://get.docker.com | sh
        log_success "Docker 安装完成"
    fi
fi

# ============================================================
# 检查 Docker Compose
# ============================================================
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version | awk '{print $3}' | sed 's/,//')
    log_success "Docker Compose 已安装: $COMPOSE_VERSION"
else
    log_warning "Docker Compose 未安装"
    log_info "Docker Compose v2 已集成到 docker 中，使用 'docker compose' 命令"
fi

# ============================================================
# 检查 Java
# ============================================================
print_section "检查 Java"

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1"."$2}')
    log_success "Java 已安装: $JAVA_VERSION"

    if [[ $(echo "$JAVA_VERSION >= 21" | bc -l 2>/dev/null || echo "0") -eq 1 ]]; then
        log_success "Java 版本符合要求 (>= 21)"
    else
        log_warning "Java 版本过低，需要 21+"
    fi
else
    log_warning "Java 未安装"
    log_info "Docker 部署不需要本地安装 Java"
fi

# ============================================================
# 检查 MySQL
# ============================================================
print_section "检查 MySQL"

if command -v mysql &> /dev/null; then
    MYSQL_VERSION=$(mysql --version | awk '{print $5}' | sed 's/,$//')
    log_success "MySQL 已安装: $MYSQL_VERSION"

    # 检查 MySQL 服务状态
    if systemctl is-active --quiet mysql || systemctl is-active --quiet mysqld; then
        log_success "MySQL 服务正在运行"
    else
        log_warning "MySQL 服务未运行"
    fi
else
    log_warning "MySQL 未安装"
    log_info "Docker 部署会在容器中运行 MySQL"
fi

# ============================================================
# 检查 Redis
# ============================================================
print_section "检查 Redis"

if command -v redis-server &> /dev/null; then
    REDIS_VERSION=$(redis-server --version | awk '{print $3}')
    log_success "Redis 已安装: $REDIS_VERSION"

    # 检查 Redis 服务状态
    if systemctl is-active --quiet redis-server || systemctl is-active --quiet redis; then
        log_success "Redis 服务正在运行"
    else
        log_warning "Redis 服务未运行"
    fi
else
    log_warning "Redis 未安装"
    log_info "Docker 部署会在容器中运行 Redis"
fi

# ============================================================
# 检查 Nginx
# ============================================================
print_section "检查 Nginx"

if command -v nginx &> /dev/null; then
    NGINX_VERSION=$(nginx -v 2>&1 | awk -F '/' '{print $2}')
    log_success "Nginx 已安装: $NGINX_VERSION"

    # 检查 Nginx 服务状态
    if systemctl is-active --quiet nginx; then
        log_success "Nginx 服务正在运行"
    else
        log_warning "Nginx 服务未运行"
    fi
else
    log_warning "Nginx 未安装"
    log_info "Docker 部署会在容器中运行 Nginx"
fi

# ============================================================
# 检查 Git
# ============================================================
print_section "检查 Git"

if command -v git &> /dev/null; then
    GIT_VERSION=$(git --version | awk '{print $3}')
    log_success "Git 已安装: $GIT_VERSION"
else
    log_warning "Git 未安装"
    read -p "是否安装 Git? (yes/no): " INSTALL_GIT
    if [ "$INSTALL_GIT" = "yes" ]; then
        apt-get update && apt-get install -y git
        log_success "Git 安装完成"
    fi
fi

# ============================================================
# 检查防火墙
# ============================================================
print_section "检查防火墙"

if command -v ufw &> /dev/null; then
    log_info "UFW 防火墙状态:"
    ufw status || echo "UFW 未启用"
else
    log_warning "UFW 未安装"
fi

# ============================================================
# 检查端口占用
# ============================================================
print_section "检查端口占用"

PORTS=(80 443 3306 6379 8081)
for PORT in "${PORTS[@]}"; do
    if ss -tlnp | grep -q ":$PORT "; then
        PROCESS=$(ss -tlnp | grep ":$PORT " | awk '{print $6}' | head -1)
        log_warning "端口 $PORT 已被占用: $PROCESS"
    else
        log_success "端口 $PORT 可用"
    fi
done

# ============================================================
# 检查项目目录
# ============================================================
print_section "检查项目目录"

PROJECT_DIR="/var/www/news-app"
if [ -d "$PROJECT_DIR" ]; then
    log_success "项目目录存在: $PROJECT_DIR"
    ls -la "$PROJECT_DIR" | head -10
else
    log_warning "项目目录不存在"
    read -p "是否创建项目目录? (yes/no): " CREATE_DIR
    if [ "$CREATE_DIR" = "yes" ]; then
        mkdir -p "$PROJECT_DIR"/{logs/{backend,nginx},backups,nginx/conf.d,nginx/ssl}
        log_success "项目目录创建完成"
    fi
fi

# ============================================================
# 检查 SSH 密钥
# ============================================================
print_section "检查 SSH 密钥"

if [ -f ~/.ssh/id_rsa ]; then
    log_success "SSH 私钥存在"
    log_info "公钥内容 (用于 GitHub Secrets):"
    echo "----------------------------------------"
    cat ~/.ssh/id_rsa
    echo "----------------------------------------"
else
    log_warning "SSH 密钥不存在"
    read -p "是否生成 SSH 密钥? (yes/no): " GEN_KEY
    if [ "$GEN_KEY" = "yes" ]; then
        ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa -N ""
        log_success "SSH 密钥生成完成"
        log_info "公钥:"
        cat ~/.ssh/id_rsa.pub
    fi
fi

# ============================================================
# 系统资源检查
# ============================================================
print_section "系统资源检查"

# 内存
MEM_TOTAL=$(free -m | grep Mem | awk '{print $2}')
MEM_USED=$(free -m | grep Mem | awk '{print $3}')
MEM_PERCENT=$((MEM_USED * 100 / MEM_TOTAL))
log_info "内存使用: ${MEM_USED}MB / ${MEM_TOTAL}MB (${MEM_PERCENT}%)"

# 磁盘
DISK_USAGE=$(df -h / | tail -1 | awk '{print $5}' | sed 's/%//')
log_info "磁盘使用: ${DISK_USAGE}%"

if [ $DISK_USAGE -gt 80 ]; then
    log_warning "磁盘使用率超过 80%"
fi

# ============================================================
# Docker 部署建议
# ============================================================
print_section "Docker 部署建议"

echo ""
log_info "对于 Docker 部署，建议:"
echo "  1. 不需要本地安装 Java、MySQL、Redis"
echo "  2. 这些服务将在 Docker 容器中运行"
echo "  3. 只需要 Docker 和 Docker Compose"
echo "  4. Nginx 也可以在容器中运行"
echo ""

# ============================================================
# 完成
# ============================================================
print_section "检查完成"

log_success "环境检查完成！"
echo ""
echo "下一步:"
echo "  1. 如果需要安装缺失的软件，请运行相应的安装命令"
echo "  2. 配置 GitHub Secrets（需要 SSH 公钥）"
echo "  3. 运行部署脚本进行首次部署"
echo ""
