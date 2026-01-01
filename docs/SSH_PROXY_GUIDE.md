# SSH 代理中转配置指南

**目的**: 让 VPS 通过 SSH 代理服务器访问 GitHub

---

## 方案概述

```
本地电脑 ──> SSH 代理服务器 ──> GitHub
             (中转)

VPS ──> SSH 代理服务器 ──> GitHub
        (通过 SSH 隧道)
```

---

## 方法 1: 配置 SSH Config (推荐)

### 1.1 在 VPS 上配置 SSH Config

```bash
# SSH 到 VPS
ssh root@139.224.189.183

# 编辑 SSH 配置文件
mkdir -p ~/.ssh
nano ~/.ssh/config
```

添加以下内容：

```ssh
# ============================================================
# SSH 代理中转配置
# ============================================================

# GitHub 通过代理
Host github.com
    HostName github.com
    User git
    Port 22
    # 方式1: 使用 ProxyJump (推荐 OpenSSH 7.3+)
    ProxyJump proxy-server

    # 方式2: 使用 nc (netcat) 如果 ProxyJump 不可用
    # ProxyCommand ssh -W %h:%p proxy-server

# ============================================================
# 代理服务器配置
# ============================================================
Host proxy-server
    HostName <你的代理服务器IP>
    User <代理服务器用户名>
    Port <代理服务器SSH端口，默认22>
    # 如果代理服务器使用密钥认证
    IdentityFile ~/.ssh/proxy_key
    # 如果使用密码认证，会在连接时提示输入
```

**替换以下内容**:
- `<你的代理服务器IP>` - 你的代理服务器地址
- `<代理服务器用户名>` - 登录代理服务器的用户名
- `<代理服务器SSH端口>` - 通常是 22
- 如果代理服务器使用密钥认证，需要上传密钥到 VPS

### 1.2 测试 SSH 连接

```bash
# 测试通过代理连接 GitHub
ssh -T git@github.com

# 如果成功，会显示：
# Hi username! You've successfully authenticated...
```

---

## 方法 2: 使用 SOCKS 代理

### 2.1 在 VPS 上创建 SSH 隧道

```bash
# SSH 到 VPS
ssh root@139.224.189.183

# 创建 SOCKS 代理隧道（在后台运行）
ssh -D 1080 -N -f <代理服务器用户名>@<代理服务器IP>

# 参数说明：
# -D 1080: 创建 SOCKS 代理，监听本地 1080 端口
# -N: 不执行远程命令，仅做端口转发
# -f: 后台运行
```

### 2.2 配置 Git 使用 SOCKS 代理

```bash
# 配置 git 使用 SOCKS 代理
git config --global http.proxy socks5://127.0.0.1:1080
git config --global https.proxy socks5://127.0.0.1:1080

# 测试克隆
git clone git@github.com:liangruchao/news-subscription-app.git

# 如果需要取消代理
# git config --global --unset http.proxy
# git config --global --unset https.proxy
```

---

## 方法 3: 在部署脚本中配置代理

### 3.1 修改部署脚本支持代理

创建 `deploy/ssh-proxy.conf` 文件：

```bash
# SSH 代理配置
PROXY_SERVER="<代理服务器IP>"
PROXY_USER="<代理服务器用户名>"
PROXY_PORT="<代理服务器端口，默认22>"

# 如果使用密钥认证
PROXY_KEY_FILE="~/.ssh/proxy_key"

# 本地 SOCKS 代理端口
SOCKS_PROXY_PORT=1080
```

### 3.2 创建代理管理脚本

`scripts/setup-proxy.sh`:

```bash
#!/bin/bash

# ============================================================
# SSH 隧道管理脚本
# ============================================================

PROXY_SERVER=$1
PROXY_USER=$2
SOCKS_PORT=${3:-1080}

PID_FILE=/tmp/ssh-tunnel.pid

start_tunnel() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "SSH 隧道已在运行 (PID: $PID)"
            return 0
        else
            rm -f $PID_FILE
        fi
    fi

    echo "启动 SSH 隧道..."
    ssh -D $SOCKS_PORT -N -f $PROXY_USER@$PROXY_SERVER
    echo $! > $PID_FILE
    echo "SSH 隧道已启动，监听端口 $SOCKS_PORT"
}

stop_tunnel() {
    if [ ! -f "$PID_FILE" ]; then
        echo "SSH 隧道未运行"
        return 0
    fi

    PID=$(cat $PID_FILE)
    kill $PID 2>/dev/null
    rm -f $PID_FILE
    echo "SSH 隧道已停止"
}

status_tunnel() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo "SSH 隧道运行中 (PID: $PID)"
            return 0
        fi
    fi
    echo "SSH 隧道未运行"
}

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
    *)
        echo "使用方法: $0 {start|stop|status|restart} <proxy_server> <proxy_user> [socks_port]"
        exit 1
        ;;
esac
```

---

## 方法 4: Docker 构建时使用代理

### 4.1 在 docker-compose.yml 中配置代理

```yaml
services:
  backend:
    build:
      context: ..
      dockerfile: Dockerfile
      args:
        # 构建时代理配置
        http_proxy: http://代理服务器IP:端口
        https_proxy: http://代理服务器IP:端口
        no_proxy: localhost,127.0.0.1
    environment:
      # 运行时代理配置
      http_proxy: http://代理服务器IP:端口
      https_proxy: http://代理服务器IP:端口
      no_proxy: localhost,127.0.0.1,mysql,redis
```

---

## 方法 5: 使用 HTTP 代理

如果你的代理服务器支持 HTTP 代理：

### 5.1 在 VPS 上配置环境变量

```bash
# 添加到 ~/.bashrc 或 ~/.profile
export http_proxy="http://代理服务器IP:端口"
export https_proxy="http://代理服务器IP:端口"
export no_proxy="localhost,127.0.0.1,本地IP"

# 使配置生效
source ~/.bashrc
```

### 5.2 配置 APT 使用代理（如果需要安装软件）

```bash
# 创建代理配置
sudo nano /etc/apt/apt.conf.d/proxy.conf

# 添加内容：
Acquire::http::Proxy "http://代理服务器IP:端口";
Acquire::https::Proxy "http://代理服务器IP:端口";
```

---

## 推荐方案

### 对于 CI/CD 部署场景：

**方案 A: SSH Config + ProxyJump (推荐)**

```bash
# 1. 在 VPS 上配置 ~/.ssh/config
# 2. 使用 ProxyJump 自动通过代理连接 GitHub
# 3. 无需修改现有部署脚本
```

**方案 B: SOCKS 隧道 + Git 配置**

```bash
# 1. 启动 SSH 隧道: ssh -D 1080 -N -f user@proxy-server
# 2. 配置 git: git config --global http.proxy socks5://127.0.0.1:1080
# 3. 正常使用部署脚本
```

---

## 下一步操作

请提供以下信息，我将为你生成具体的配置文件：

1. **代理服务器类型**: 是 SSH 服务器还是 HTTP/SOCKS 代理？
2. **代理服务器地址**: IP 或域名
3. **认证方式**: 密钥还是密码？
4. **端口**: SSH 端口（默认22）或代理端口

提供这些信息后，我会为你生成完整的配置文件和自动化脚本。
