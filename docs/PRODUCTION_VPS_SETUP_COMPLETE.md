# Production VPS CI/CD 环境配置完成报告

**配置日期**: 2026-01-02
**VPS 地址**: 139.224.189.183
**代理服务器**: 206.237.4.197:22

---

## 概述

本文档记录了 Production VPS 的完整 CI/CD 环境配置过程，包括 Docker 安装、SSH 代理中转配置、GitHub SSH 密钥配置等。

---

## 系统信息

| 组件 | 版本/配置 |
|------|----------|
| 操作系统 | Ubuntu 22.04.5 LTS |
| 内核版本 | 5.15.0-161-generic |
| CPU | 2 核心 |
| 内存 | 3.6GB |
| 磁盘 | 35GB 可用 |
| Docker | 28.4.0 |
| Docker Compose | v2 (集成) |

---

## 配置步骤

### 步骤 1: Docker 环境准备

**目标**: 确保 Docker 环境可用

```bash
# 验证 Docker 安装
docker --version
docker-compose --version

# 创建项目目录
mkdir -p /var/www/news-app/{logs/{backend,nginx},backups,nginx/conf.d,nginx/ssl}
```

**结果**: ✅ Docker 已安装

---

### 步骤 2: 配置 SSH 代理中转

**问题**: VPS 无法直接访问 GitHub

**解决方案**: 通过代理服务器 (206.237.4.197:22) 建立 SSH 隧道

#### 2.1 修复 APT 源问题

```bash
# 删除有问题的 Docker 源
rm -f /etc/apt/sources.list.d/download.docker.list

# 更新软件源
apt-get update
```

#### 2.2 安装必要工具

```bash
# 安装 sshpass (用于密码认证)
apt-get install -y sshpass

# 安装 netcat
apt-get install -y netcat
```

#### 2.3 创建 SOCKS 隧道脚本

创建 `~/start-ssh-tunnel.sh`:

```bash
#!/bin/bash
PROXY_SERVER="206.237.4.197"
PROXY_USER="root"
PROXY_PORT="22"
SOCKS_PORT="1080"

# 使用 sshpass 启动隧道
sshpass -p 'XpF5d99dGThaixM' ssh -D $SOCKS_PORT -N -f \
    -o StrictHostKeyChecking=no \
    -o UserKnownHostsFile=/dev/null \
    $PROXY_USER@$PROXY_SERVER
```

创建 `~/stop-ssh-tunnel.sh`:

```bash
#!/bin/bash
SOCKS_PORT="1080"
PROXY_SERVER="206.237.4.197"
pkill -f "ssh -D $SOCKS_PORT.*$PROXY_SERVER"
```

#### 2.4 配置 Git 使用代理

```bash
git config --global http.proxy socks5://127.0.0.1:1080
git config --global https.proxy socks5://127.0.0.1:1080
```

#### 2.5 配置开机自启动 (systemd)

创建 `/etc/systemd/system/ssh-tunnel.service`:

```ini
[Unit]
Description=SSH Tunnel to Proxy Server
After=network.target

[Service]
Type=forking
User=root
ExecStart=/root/start-ssh-tunnel.sh
ExecStop=/root/stop-ssh-tunnel.sh
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
systemctl daemon-reload
systemctl enable ssh-tunnel.service
systemctl start ssh-tunnel.service
```

**结果**: ✅ SSH 隧道已配置并运行

---

### 步骤 3: 配置 GitHub SSH 密钥

**问题**: Git SSH 连接 GitHub 时权限被拒绝

**解决方案**: 生成 SSH 密钥并添加到 GitHub

#### 3.1 生成 SSH 密钥对

```bash
# 生成 4096 位 RSA 密钥
ssh-keygen -t rsa -b 4096 -C "root@production-vps-20260102" -f ~/.ssh/id_rsa -N ""
```

#### 3.2 获取公钥

```bash
# 显示公钥
cat ~/.ssh/id_rsa.pub
```

公钥内容：
```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQC... root@production-vps-20260102
```

#### 3.3 添加到 GitHub

**方式 A: Deploy Key（推荐）**

1. 访问: https://github.com/liangruchao/news-subscription-app/settings/keys
2. 点击 **New deploy key**
3. 标题: `Production VPS (139.224.189.183)`
4. 粘贴公钥
5. 点击 **Add deploy key**

**方式 B: 账户 SSH Key**

1. 访问: https://github.com/settings/keys
2. 点击 **New SSH key**
3. 标题: `Production VPS`
4. 粘贴公钥
5. 点击 **Add SSH key**

#### 3.4 配置 SSH Config

编辑 `~/.ssh/config`:

```ssh
# GitHub 配置
Host github.com
    HostName github.com
    User git
    Port 22
    IdentityFile ~/.ssh/id_rsa
```

#### 3.5 测试连接

```bash
# 测试 SSH 连接
ssh -T git@github.com

# 成功输出:
# Hi liangruchao! You've successfully authenticated...
```

**结果**: ✅ GitHub SSH 密钥配置完成

---

### 步骤 4: 配置 Git

#### 4.1 配置用户信息

```bash
git config --global user.name "Production VPS"
git config --global user.email "prod-vps@139.224.189.183"
```

#### 4.2 取消 HTTP 代理（使用 SSH）

```bash
# 清除 HTTP 代理配置（Git 使用 SSH）
git config --global --unset http.proxy
git config --global --unset https.proxy
```

---

## 配置文件清单

| 文件路径 | 说明 |
|----------|------|
| `~/.ssh/config` | SSH 配置文件 |
| `~/.ssh/id_rsa` | SSH 私钥 |
| `~/.ssh/id_rsa.pub` | SSH 公钥 |
| `~/start-ssh-tunnel.sh` | 启动 SSH 隧道脚本 |
| `~/stop-ssh-tunnel.sh` | 停止 SSH 隧道脚本 |
| `~/tunnel-status.sh` | 隧道状态检查脚本 |
| `/etc/systemd/system/ssh-tunnel.service` | systemd 服务配置 |

---

## 管理命令

### SSH 隧道管理

```bash
# 启动隧道
~/start-ssh-tunnel.sh

# 停止隧道
~/stop-ssh-tunnel.sh

# 查看状态
~/tunnel-status.sh

# 或使用 systemd
systemctl start ssh-tunnel
systemctl stop ssh-tunnel
systemctl status ssh-tunnel
```

### Git 操作

```bash
# 克隆仓库（通过 SSH）
git clone git@github.com:liangruchao/news-subscription-app.git

# 测试连接
ssh -T git@github.com
```

---

## 网络架构

```
Production VPS (139.224.189.183)
    │
    │ SSH 隧道 (SOCKS 1080)
    │
    ▼
代理服务器 (206.237.4.197:22)
    │
    │ SSH 连接
    ▼
GitHub (github.com)
```

---

## 故障排查

### 问题 1: SSH 隧道无法启动

**症状**: 执行 `~/start-ssh-tunnel.sh` 后隧道没有运行

**解决方案**:
```bash
# 检查进程
ps aux | grep "ssh -D 1080"

# 检查端口
netstat -tlnp | grep 1080

# 查看日志
journalctl -u ssh-tunnel -n 50
```

### 问题 2: Git 无法访问 GitHub

**症状**: `git clone` 失败

**解决方案**:
```bash
# 检查 SSH 隧道
~/tunnel-status.sh

# 测试 SSH 连接
ssh -T git@github.com

# 测试代理连接
curl -x socks5://127.0.0.1:1080 https://api.github.com
```

### 问题 3: 代理服务器连接失败

**症状**: 隧道启动失败

**解决方案**:
```bash
# 手动测试代理服务器连接
ssh root@206.237.4.197

# 检查密码认证
sshpass -p 'XpF5d99dGThaixM' ssh root@206.237.4.197 echo "connected"
```

---

## 下一步

### 短期

1. ✅ 测试 Git 克隆仓库
2. ⏳ 配置 GitHub Secrets
3. ⏳ 运行首次部署

### 中期

1. 配置 Staging 环境代理
2. 测试完整的 CI/CD 流程
3. 配置监控和日志

### 长期

1. 设置代理服务器密钥认证（更安全）
2. 配置负载均衡和备份
3. 实现自动化监控告警

---

## 附录

### A. GitHub Secrets 配置清单

在 GitHub 仓库 → Settings → Secrets and variables → Actions 中添加：

| Secret | 值 |
|--------|---|
| `STAGING_VPS_HOST` | 47.103.204.114 |
| `STAGING_VPS_USER` | root |
| `STAGING_VPS_SSH_KEY` | (47.103.204.114 的私钥) |
| `PRODUCTION_VPS_HOST` | 139.224.189.183 |
| `PRODUCTION_VPS_USER` | root |
| `PRODUCTION_VPS_SSH_KEY` | (139.224.189.183 的私钥) |

### B. 端口使用情况

| 端口 | 用途 | 状态 |
|------|------|------|
| 80 | HTTP | 可用 |
| 443 | HTTPS | 可用 |
| 3306 | MySQL | Docker 内部 |
| 6379 | Redis | Docker 内部 |
| 8081 | Backend | Docker 内部 |
| 1080 | SOCKS 代理 | 已使用 |

---

**配置状态**: ✅ 完成
**最后更新**: 2026-01-02
**维护**: root@139.224.189.183
