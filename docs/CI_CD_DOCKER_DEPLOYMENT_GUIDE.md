# CI/CD 和 Docker 容器化部署完整指南

**创建日期**: 2026-01-02
**目的**: 提供完整的 CI/CD 和 Docker 部署方案

---

## 目录

1. [本地 CI/CD 方案](#一本地-cicd-方案)
2. [Docker 本地开发环境](#二docker-本地开发环境)
3. [Docker 生产环境部署](#三docker-生产环境部署)
4. [GitHub Actions CI/CD](#四github-actions-cicd)
5. [故障排查](#五故障排查)

---

## 一、本地 CI/CD 方案

### 1.1 概述

由于网络环境限制，本地 CI/CD 脚本提供了完整的替代方案：

| 特性 | 说明 |
|------|------|
| 完全本地执行 | 不受网络限制 |
| 功能完整 | 测试、构建、部署 |
| 多环境支持 | dev、staging、production |

### 1.2 快速开始

```bash
# 1. 给脚本添加执行权限
chmod +x scripts/*.sh

# 2. 运行本地 CI (开发环境)
./scripts/local-ci.sh dev

# 3. 运行本地 CI (测试环境)
./scripts/local-ci.sh staging

# 4. 运行本地 CI (生产环境)
./scripts/local-ci.sh production
```

### 1.3 CI 流程说明

**本地 CI 包含以下步骤**:

| 步骤 | 说明 | 命令 |
|------|------|------|
| 1 | 环境检查 | 检查 Java、Maven、Node.js 版本 |
| 2 | 代码检查 | 检查 Git 状态 |
| 3 | 后端单元测试 | `mvn test -DskipITs=true` |
| 4 | 后端集成测试 | `mvn verify -DskipUnitTests=true` |
| 5 | 代码覆盖率 | `mvn jacoco:report` |
| 6 | 前端测试 | `npm run test` |
| 7 | 构建应用 | `mvn clean package` |
| 8 | 部署 | (仅 staging/production) |

### 1.4 集成到 Git Hooks

```bash
# 安装 pre-commit hook (每次提交前运行测试)
cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
echo "运行快速检查..."
cd backend && mvn test -DskipITs=true -q
EOF
chmod +x .git/hooks/pre-commit

# 安装 pre-push hook (每次推送前运行完整 CI)
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash
echo "运行本地 CI..."
./scripts/local-ci.sh dev
EOF
chmod +x .git/hooks/pre-push
```

---

## 二、Docker 本地开发环境

### 2.1 前置要求

```bash
# 检查 Docker 是否安装
docker --version
# 需要 Docker 20.10+

# 检查 Docker Compose 是否安装
docker-compose --version
# 需要 Docker Compose 2.0+
```

### 2.2 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件
vim .env
```

**必需配置** (.env 文件):
```bash
# NewsAPI Key (必须配置!)
NEWSAPI_API_KEY=你的API_Key

# MySQL 密码 (可选，默认值已配置)
MYSQL_PASSWORD=NewsApp@2026DB

# Redis 密码 (可选，默认值已配置)
REDIS_PASSWORD=Redis@2026SecurePass
```

### 2.3 启动开发环境

```bash
# 启动所有服务（包含 phpMyAdmin 和 Redis Commander）
docker-compose --profile development up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

### 2.4 服务访问地址

| 服务 | URL | 说明 |
|------|-----|------|
| 后端 API | http://localhost:8081/api | Spring Boot |
| 前端页面 | http://localhost:8080 | 使用前端开发服务器 |
| phpMyAdmin | http://localhost:8080 | MySQL 管理界面 |
| Redis Commander | http://localhost:8081 | Redis 管理界面 |

### 2.5 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启服务
docker-compose restart backend

# 查看日志
docker-compose logs -f [service_name]

# 进入容器
docker-compose exec backend bash

# 查看资源使用
docker stats

# 清理未使用的镜像和容器
docker system prune -a

# 重新构建镜像
docker-compose build --no-cache backend
```

### 2.6 开发工作流

```bash
# 1. 启动 Docker 服务
docker-compose --profile development up -d

# 2. 等待服务就绪（大约 30 秒）
docker-compose logs -f backend
# 看到 "Started NewsApplication" 表示启动成功

# 3. 运行测试
./scripts/local-ci.sh dev

# 4. 开发完成后停止服务
docker-compose down
```

---

## 三、Docker 生产环境部署

### 3.1 VPS 环境准备

```bash
# SSH 连接到 VPS
ssh root@47.103.204.114

# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 安装 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

### 3.2 部署到 VPS

```bash
# 在本地执行 - 上传项目文件到 VPS
scp docker-compose.yml Dockerfile .env root@47.103.204.114:/var/www/news-app/
scp -r nginx root@47.103.204.114:/var/www/news-app/

# SSH 到 VPS
ssh root@47.103.204.114

# 进入应用目录
cd /var/www/news-app

# 启动生产环境（不包含开发工具）
docker-compose --profile production up -d

# 查看服务状态
docker-compose ps
```

### 3.3 生产环境服务

```bash
# 查看运行的服务
docker-compose ps

# 预期输出:
# NAME                STATUS              PORTS
# news-mysql          Up (healthy)        0.0.0.0:3306->3306/tcp
# news-redis          Up (healthy)        0.0.0.0:6379->6379/tcp
# news-backend        Up (healthy)        0.0.0.0:8081->8081/tcp
# news-nginx          Up                  0.0.0.0:80->80/tcp
```

### 3.4 生产环境访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://47.103.204.114/ | 主页 |
| 注册页面 | http://47.103.204.114/register.html | 注册 |
| 登录页面 | http://47.103.204.114/login.html | 登录 |
| API 接口 | http://47.103.204.114/api/* | 后端 API |

### 3.5 生产环境维护

```bash
# 查看日志
docker-compose logs -f backend
docker-compose logs -f nginx

# 重启服务
docker-compose restart backend

# 更新应用
docker-compose down
docker-compose pull
docker-compose --profile production up -d

# 备份数据
docker-compose exec mysql mysqldump -u newsapp -p news_app > backup.sql

# 恢复数据
docker-compose exec -T mysql mysql -u newsapp -p news_app < backup.sql
```

---

## 四、GitHub Actions CI/CD

### 4.1 概述

GitHub Actions 配置文件位于 `.github/workflows/ci.yml`，包含以下任务：

| 任务 | 说明 |
|------|------|
| backend-unit-tests | 后端单元测试 |
| backend-integration-tests | 后端集成测试 |
| frontend-unit-tests | 前端单元测试 |
| e2e-tests | E2E 测试 |
| code-quality | 代码质量检查 |
| build-and-deploy | 构建和部署 |

### 4.2 触发条件

```yaml
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
```

### 4.3 配置 GitHub Secrets (可选)

如果要启用自动部署，需要配置以下 Secrets：

| Secret | 说明 | 示例 |
|--------|------|------|
| VPS_HOST | VPS 主机地址 | 47.103.204.114 |
| VPS_USER | SSH 用户名 | root |
| VPS_KEY | SSH 私钥 | -----BEGIN RSA... |
| NEWSAPI_API_KEY | NewsAPI 密钥 | your_api_key |

**配置步骤**:
1. 进入 GitHub 仓库
2. Settings → Secrets and variables → Actions
3. 点击 "New repository secret" 添加

### 4.4 查看流水线状态

```
GitHub 仓库 → Actions 标签 → 选择 workflow run
```

---

## 五、故障排查

### 5.1 本地 CI 失败

**问题**: 测试失败
```bash
# 查看详细日志
cat logs/backend-unit-test-*.log

# 单独运行失败的测试
cd backend
mvn test -Dtest=ClassName#methodName
```

**问题**: 构建失败
```bash
# 清理并重新构建
cd backend
mvn clean package -U
```

### 5.2 Docker 部署失败

**问题**: 容器无法启动
```bash
# 查看容器日志
docker-compose logs backend

# 检查容器状态
docker-compose ps

# 重新构建镜像
docker-compose build --no-cache backend
```

**问题**: 数据库连接失败
```bash
# 检查 MySQL 容器
docker-compose logs mysql

# 进入容器检查
docker-compose exec mysql mysql -u newsapp -p

# 检查网络连接
docker-compose exec backend ping mysql
```

**问题**: 端口冲突
```bash
# 检查端口占用
netstat -tulpn | grep :8081

# 修改 docker-compose.yml 中的端口映射
ports:
  - "8082:8081"  # 使用 8082 端口
```

### 5.3 VPS 部署失败

**问题**: 服务启动失败
```bash
# SSH 到 VPS 查看日志
ssh root@47.103.204.114 'cd /var/www/news-app && docker-compose logs backend'
```

**问题**: 内存不足
```bash
# 查看内存使用
free -h

# 减少内存限制 (docker-compose.yml)
environment:
  - JAVA_OPTS=-Xms128m -Xmx512m
```

---

## 六、快速参考

### 6.1 常用命令

```bash
# 本地开发
mvn spring-boot:run              # 启动后端
npm run dev                       # 启动前端

# 本地测试
./scripts/local-ci.sh dev         # 运行本地 CI
mvn test                          # 运行后端测试
npm run test                      # 运行前端测试

# Docker 部署
docker-compose up -d              # 启动所有服务
docker-compose logs -f            # 查看日志
docker-compose down               # 停止所有服务

# VPS 管理
ssh root@47.103.204.114           # SSH 连接
docker-compose ps                 # 查看服务状态
docker-compose restart backend    # 重启后端
```

### 6.2 端口占用情况

| 端口 | 服务 | 说明 |
|------|------|------|
| 8081 | 后端 | Spring Boot API |
| 8080 | 前端/管理工具 | 静态文件、phpMyAdmin |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 80 | Nginx | Web 服务器 (VPS) |

### 6.3 环境配置文件对照

| 文件 | 环境 | 使用场景 |
|------|------|---------|
| `application.properties` | 默认 | 本地开发 |
| `application-test.properties` | test | 单元测试/集成测试 |
| `application-vps.properties` | vps | VPS 直接部署 |
| `application-docker.properties` | docker | Docker 部署 |

### 6.4 项目文件结构

```
.
├── .github/workflows/
│   └── ci.yml                    # GitHub Actions CI/CD 配置
├── docker-compose.yml            # Docker Compose 编排
├── Dockerfile                    # 后端镜像构建
├── .env.example                  # 环境变量模板
├── scripts/
│   ├── local-ci.sh               # 本地 CI 脚本
│   ├── deploy-vps.sh             # VPS 部署脚本
│   └── deploy-frontend.sh        # 前端部署脚本
├── nginx/
│   ├── nginx.conf                # Nginx 主配置
│   ├── conf.d/
│   │   └── default.conf          # 站点配置
│   ├── ssl/                      # SSL 证书目录
│   └── logs/                     # 日志目录
├── backend/
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-test.properties
│   │   ├── application-docker.properties
│   │   └── application-vps.properties
│   └── pom.xml
└── frontend/
    ├── package.json
    ├── vitest.config.js
    └── playwright.config.js
```

---

**状态**: ✅ CI/CD 和 Docker 部署方案已完成
**最后更新**: 2026-01-02
