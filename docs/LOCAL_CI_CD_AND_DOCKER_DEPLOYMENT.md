# 本地 CI/CD 和 Docker 部署指南

**创建日期**: 2026-01-02
**目的**: 提供不依赖 GitHub Actions 的本地 CI/CD 方案和 Docker 部署方案

---

## 一、本地 CI/CD 方案

### 1.1 为什么需要本地 CI/CD？

由于网络环境限制，GitHub Actions 可能无法正常工作。本地 CI/CD 脚本提供了完整的替代方案：

**优势**:
- ✅ 完全本地执行，不受网络限制
- ✅ 功能完整：测试、构建、部署
- ✅ 可以集成到本地 Git hooks
- ✅ 支持多环境部署（dev/staging/production）

### 1.2 脚本文件

| 文件 | 用途 |
|------|------|
| `scripts/local-ci.sh` | 本地 CI/CD 主脚本 |
| `scripts/deploy-vps.sh` | VPS 自动部署脚本 |
| `scripts/deploy-frontend.sh` | 前端自动部署脚本 |

### 1.3 使用本地 CI/CD

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

### 1.4 CI 流程说明

**本地 CI 包含以下步骤**:

1. **环境检查**: 检查 Java、Maven、Node.js 版本
2. **代码检查**: 检查 Git 状态
3. **后端单元测试**: `mvn test -DskipITs=true`
4. **后端集成测试**: `mvn verify -DskipUnitTests=true`
5. **代码覆盖率**: `mvn jacoco:report`
6. **前端测试**: `npm run test`
7. **构建应用**: `mvn clean package`
8. **部署** (staging/production): 自动部署到 VPS

### 1.5 自动触发 CI

可以将本地 CI 集成到 Git hooks 中：

```bash
# 安装 pre-push hook (每次推送前运行 CI)
cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash
echo "运行本地 CI..."
./scripts/local-ci.sh dev
EOF

chmod +x .git/hooks/pre-push
```

---

## 二、Docker 部署方案

### 2.1 Docker 文件说明

| 文件 | 用途 |
|------|------|
| `Dockerfile` | 后端应用镜像构建 |
| `docker-compose.yml` | 完整的应用栈编排 |
| `.env.example` | 环境变量模板 |
| `application-docker.properties` | Docker 环境配置 |

### 2.2 本地开发环境 (使用 Docker)

#### 步骤 1: 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，修改必要配置
vim .env
```

#### 步骤 2: 启动开发环境

```bash
# 启动所有服务（包含 phpMyAdmin 和 Redis Commander）
docker-compose --profile development up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

#### 步骤 3: 访问服务

| 服务 | URL | 说明 |
|------|-----|------|
| 后端 API | http://localhost:8081/api | Spring Boot |
| 前端页面 | http://localhost:8080 | 使用前端开发服务器 |
| phpMyAdmin | http://localhost:8080 | MySQL 管理界面 |
| Redis Commander | http://localhost:8081 | Redis 管理界面 |

### 2.3 VPS 生产环境 (使用 Docker)

#### 步骤 1: 在 VPS 上安装 Docker

```bash
# SSH 连接到 VPS
ssh root@47.103.204.114

# 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# 安装 Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### 步骤 2: 上传项目文件

```bash
# 在本地执行
scp docker-compose.yml Dockerfile .env root@47.103.204.114:/var/www/news-app/
```

#### 步骤 3: 启动生产环境

```bash
# SSH 到 VPS
ssh root@47.103.204.114

# 进入应用目录
cd /var/www/news-app

# 启动生产环境（不包含开发工具）
docker-compose --profile production up -d

# 查看服务状态
docker-compose ps
```

### 2.4 Docker 常用命令

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 查看日志
docker-compose logs -f [service_name]

# 进入容器
docker-compose exec backend bash

# 查看资源使用
docker stats

# 清理未使用的镜像和容器
docker system prune -a
```

---

## 三、环境配置文件说明

### 3.1 环境配置文件对照

| 文件 | 环境 | 使用场景 |
|------|------|---------|
| `application.properties` | 默认 | 本地开发 |
| `application-test.properties` | test | 单元测试/集成测试 |
| `application-vps.properties` | vps | VPS 直接部署 |
| `application-docker.properties` | docker | Docker 部署 |

### 3.2 VPS 部署配置差异

**VPS 直接部署** (当前方案):
- 使用 Systemd 管理服务
- 直接安装 Java/MySQL/Redis
- 使用 Nginx 反向代理
- 适合小型项目，资源占用小

**Docker 部署** (可选方案):
- 使用 Docker Compose 编排
- 所有服务运行在容器中
- 更好的隔离性和可移植性
- 适合中大型项目，易于扩展

---

## 四、前端部署方案

### 4.1 前端部署到 VPS

前端是静态 HTML/CSS/JS 文件，可以直接部署到 Nginx：

```bash
# 运行前端部署脚本
./scripts/deploy-frontend.sh
```

**脚本执行流程**:
1. 检查前端文件
2. 在 VPS 上创建前端目录 `/var/www/news-app/frontend`
3. 使用 rsync 上传前端文件
4. 更新 Nginx 配置（同时支持前端和后端 API）
5. 验证部署

### 4.2 前端 Nginx 配置

```nginx
# 前端静态文件服务
server {
    listen 80;
    server_name 47.103.204.114;

    # 前端根目录
    root /var/www/news-app/frontend;
    index index.html;

    # 前端静态文件
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理到后端
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 4.3 前端访问地址

部署后可以通过以下地址访问：
- **前端首页**: http://47.103.204.114/
- **注册页面**: http://47.103.204.114/register.html
- **登录页面**: http://47.103.204.114/login.html
- **API 接口**: http://47.103.204.114/api/*

---

## 五、完整部署流程

### 5.1 使用脚本部署 (推荐)

```bash
# 1. 本地 CI 和测试
./scripts/local-ci.sh staging

# 2. 部署后端到 VPS
./scripts/deploy-vps.sh production

# 3. 部署前端到 VPS
./scripts/deploy-frontend.sh
```

### 5.2 使用 Docker 部署

```bash
# 1. 本地构建和测试
docker-compose build

# 2. 启动本地环境验证
docker-compose --profile development up -d

# 3. 停止本地环境
docker-compose down

# 4. 部署到 VPS
scp docker-compose.yml Dockerfile .env root@47.103.204.114:/var/www/news-app/

# 5. 在 VPS 上启动
ssh root@47.103.204.114 'cd /var/www/news-app && docker-compose --profile production up -d'
```

---

## 六、故障排查

### 6.1 本地 CI 失败

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

### 6.2 Docker 部署失败

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
```

### 6.3 VPS 部署失败

**问题**: 服务启动失败
```bash
# SSH 到 VPS 查看日志
ssh root@47.103.204.114 'journalctl -u newsapp -n 50'

# 检查服务状态
ssh root@47.103.204.114 'systemctl status newsapp'
```

**问题**: API 无法访问
```bash
# 检查 Nginx 配置
ssh root@47.103.204.114 'nginx -t'

# 查看 Nginx 日志
ssh root@47.103.204.114 'tail -f /var/log/nginx/news-app-error.log'
```

---

## 七、快速参考

### 7.1 常用命令

```bash
# 本地开发
mvn spring-boot:run              # 启动后端
npm run dev                       # 启动前端

# 本地测试
./scripts/local-ci.sh dev         # 运行本地 CI
mvn test                          # 运行后端测试
npm run test                      # 运行前端测试

# 部署到 VPS
./scripts/deploy-vps.sh production    # 部署后端
./scripts/deploy-frontend.sh          # 部署前端

# Docker 部署
docker-compose up -d              # 启动所有服务
docker-compose logs -f            # 查看日志
docker-compose down               # 停止所有服务

# VPS 管理
ssh root@47.103.204.114 'systemctl status newsapp'    # 查看服务状态
ssh root@47.103.204.114 'journalctl -u newsapp -f'    # 查看应用日志
```

### 7.2 端口占用情况

| 端口 | 服务 | 说明 |
|------|------|------|
| 8081 | 后端 | Spring Boot API |
| 8080 | 前端/管理工具 | 静态文件、phpMyAdmin |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 80 | Nginx | Web 服务器 (VPS) |

---

**状态**: ✅ 本地 CI/CD 和 Docker 部署方案已完成
**最后更新**: 2026-01-02
