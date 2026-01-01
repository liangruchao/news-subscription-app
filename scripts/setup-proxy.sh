#!/bin/bash

# ============================================================
# SSH 隧道管理脚本
# 用途: 启动/停止/管理 SSH SOCKS 隧道
# 使用: ./scripts/setup-proxy.sh {start|stop|status|restart}
# ============================================================

set -e

# ============================================================
# 配置
# ============================================================
# 请根据实际情况修改以下配置
PROXY_SERVER="${SSH_PROXY_HOST:-your-proxy-server.com}"
PROXY_USER="${SSH_PROXY_USER:-proxy-user}"
PROXY_PORT="${SSH_PROXY_PORT:-22}"
SOCKS_PORT="${SOCKS_PORT:-1080}"

# PID 文件
PID_FILE="/tmp/ssh-tunnel.pid"

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

# ============================================================
# 函数
# ============================================================

start_tunnel() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            log_warning "SSH 隧道已在运行 (PID: $PID)"
            return 0
        else
            rm -f $PID_FILE
        fi
    fi

    log_info "启动 SSH 隧道到 $PROXY_USER@$PROXY_SERVER:$PROXY_PORT"
    log_info "SOCKS 代理监听端口: $SOCKS_PORT"

    # 启动 SSH 隧道
    ssh -D $SOCKS_PORT -N -f $PROXY_USER@$PROXY_SERVER -p $PROXY_PORT

    if [ $? -eq 0 ]; then
        # 保存 PID
        sleep 1
        PID=$(pgrep -f "ssh -D $SOCKS_PORT")
        if [ -n "$PID" ]; then
            echo $PID > $PID_FILE
            log_success "SSH 隧道已启动 (PID: $PID)"
        else
            log_error "无法获取隧道进程 PID"
            return 1
        fi
    else
        log_error "SSH 隧道启动失败"
        return 1
    fi

    log_info "使用方法:"
    log_info "  Git: git config --global http.proxy socks5://127.0.0.1:$SOCKS_PORT"
    log_info "  Curl: curl -x socks5://127.0.0.1:$SOCKS_PORT https://api.github.com"
}

stop_tunnel() {
    if [ ! -f "$PID_FILE" ]; then
        log_warning "SSH 隧道未运行 (PID 文件不存在)"
        # 尝试查找并停止进程
        pkill -f "ssh -D $SOCKS_PORT" 2>/dev/null && log_success "已停止相关进程" || true
        return 0
    fi

    PID=$(cat $PID_FILE)
    if ps -p $PID > /dev/null 2>&1; then
        kill $PID
        log_success "SSH 隧道已停止 (PID: $PID)"
    else
        log_warning "进程 $PID 不存在，清理 PID 文件"
    fi

    rm -f $PID_FILE
}

status_tunnel() {
    echo ""
    echo "=================================================================="
    echo "  SSH 隧道状态"
    echo "=================================================================="
    echo ""
    echo "代理服务器: $PROXY_USER@$PROXY_SERVER:$PROXY_PORT"
    echo "SOCKS 端口:  $SOCKS_PORT"
    echo ""

    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            log_success "SSH 隧道运行中 (PID: $PID)"
            echo ""
            echo "网络连接:"
            ss -tlnp | grep ":$SOCKS_PORT" || echo "  端口 $SOCKS_PORT 未监听"
            echo ""
            return 0
        fi
    fi

    # 检查是否有其他进程在使用该端口
    if ss -tlnp | grep -q ":$SOCKS_PORT "; then
        log_warning "端口 $SOCKS_PORT 被占用，但不是由本脚本启动"
    else
        log_warning "SSH 隧道未运行"
    fi
    echo ""
}

test_tunnel() {
    log_info "测试 SSH 隧道连接..."

    # 测试 Git 连接
    if curl -x socks5://127.0.0.1:$SOCKS_PORT -s -o /dev/null -w "%{http_code}" https://api.github.com | grep -q "200\|404\|403"; then
        log_success "通过代理可以访问 GitHub API"
    else
        log_error "无法通过代理访问 GitHub API"
        return 1
    fi

    # 测试 SSH 连接
    if GIT_SSH_COMMAND="ssh -o ProxyJump=$PROXY_USER@$PROXY_SERVER:$PROXY_PORT" git ls-remote git@github.com:liangruchao/news-subscription-app.git &>/dev/null; then
        log_success "通过 SSH 代理可以访问 GitHub"
    else
        log_warning "SSH 代理连接测试失败"
    fi
}

configure_git() {
    log_info "配置 Git 使用 SOCKS 代理..."

    git config --global http.proxy socks5://127.0.0.1:$SOCKS_PORT
    git config --global https.proxy socks5://127.0.0.1:$SOCKS_PORT

    log_success "Git 代理配置完成"
    echo ""
    echo "当前 Git 代理配置:"
    git config --global --get http.proxy
    git config --global --get https.proxy
    echo ""
}

unconfigure_git() {
    log_info "取消 Git 代理配置..."

    git config --global --unset http.proxy
    git config --global --unset https.proxy

    log_success "Git 代理配置已清除"
}

# ============================================================
# 主程序
# ============================================================

case "$1" in
    start)
        start_tunnel
        ;;
    stop)
        stop_tunnel
        ;;
    status)
        status_tunnel
        ;;
    restart)
        stop_tunnel
        sleep 1
        start_tunnel
        ;;
    test)
        test_tunnel
        ;;
    git)
        configure_git
        ;;
    ungit)
        unconfigure_git
        ;;
    *)
        echo "SSH 隧道管理脚本"
        echo ""
        echo "使用方法: $0 {start|stop|status|restart|test|git|ungit}"
        echo ""
        echo "命令说明:"
        echo "  start   - 启动 SSH 隧道"
        echo "  stop    - 停止 SSH 隧道"
        echo "  status  - 查看隧道状态"
        echo "  restart - 重启 SSH 隧道"
        echo "  test    - 测试代理连接"
        echo "  git     - 配置 Git 使用代理"
        echo "  ungit   - 取消 Git 代理配置"
        echo ""
        echo "环境变量:"
        echo "  SSH_PROXY_HOST    - 代理服务器地址 (当前: $PROXY_SERVER)"
        echo "  SSH_PROXY_USER    - 代理服务器用户 (当前: $PROXY_USER)"
        echo "  SSH_PROXY_PORT    - 代理服务器端口 (当前: $PROXY_PORT)"
        echo "  SOCKS_PORT        - 本地 SOCKS 端口 (当前: $SOCKS_PORT)"
        echo ""
        echo "示例:"
        echo "  # 使用默认配置启动"
        echo "  $0 start"
        echo ""
        echo "  # 使用自定义代理服务器"
        echo "  SSH_PROXY_HOST=myproxy.com SSH_PROXY_USER=myuser $0 start"
        echo ""
        exit 1
        ;;
esac
