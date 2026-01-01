# 双环境 CI/CD 部署方案 (Plan B)

**创建日期**: 2026-01-02
**适用场景**: 两个 VPS 环境（Staging 测试环境 + Production 生产环境）

---

## 目录

1. [架构概述](#一架构概述)
2. [环境规划](#二环境规划)
3. [本地开发到生产流程](#三本地开发到生产流程)
4. [详细配置](#四详细配置)
5. [部署脚本](#五部署脚本)
6. [CI/CD 流水线](#六cicd-流水线)
7. [回滚策略](#七回滚策略)
8. [监控和日志](#八监控和日志)

---

## 一、架构概述

### 1.1 双环境架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Git 仓库 (GitHub)                             │
│                   main / develop / feature/* 分支                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CI/CD 流水线                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐           │
│  │   构建   │→│   测试   │→│  构建    │→│  部署    │           │
│  │ (Docker)│  │ (全部)   │  │ (镜像)  │  │ (自动)   │           │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘           │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
        ┌───────────────────┐   ┌───────────────────┐
        │   Staging 环境    │   │  Production 环境   │
        │   (测试验证)      │   │   (生产运行)       │
        ├───────────────────┤   ├───────────────────┤
        │ VPS: 47.103.204.114│   │ VPS: 139.224.189.183│
        │ 域名: staging.*   │   │ 域名: www.*       │
        │ 数据: 测试数据    │   │ 数据: 真实数据    │
        │ 更新: 自动        │   │ 更新: 手动确认    │
        └───────────────────┘   └───────────────────┘
```

### 1.2 部署策略对比

| 特性 | Staging 环境 | Production 环境 |
|------|-------------|----------------|
| 部署触发 | develop 分支推送 | main 分支推送 + 手动确认 |
| 数据来源 | 测试数据/模拟数据 | 真实用户数据 |
| 访问权限 | 团队内部 | 公开访问 |
| 更新频率 | 每次提交 | 经过验证后 |
| 回滚策略 | 快速重建 | 蓝绿部署 |
| 监控级别 | 基础监控 | 完整监控 + 告警 |
| SSL 证书 | 自签名或 Let's Encrypt | Let's Encrypt 正式证书 |

---

## 二、环境规划

### 2.1 VPS 资源分配

| 环境 | VPS IP | 域名 | 用途 |
|------|--------|------|------|
| **Staging** | 47.103.204.114 | staging.yourdomain.com | 测试环境 |
| **Production** | 139.224.189.183 | www.yourdomain.com | 生产环境 |

### 2.2 端口分配（两套环境相同）

| 服务 | 容器内端口 | 宿主机端口 | 说明 |
|------|-----------|-----------|------|
| Nginx | 80 | 80 | HTTP |
| Nginx | 443 | 443 | HTTPS |
| Backend | 8081 | - | 内部访问 |
| MySQL | 3306 | - | 内部访问 |
| Redis | 6379 | - | 内部访问 |

### 2.3 数据库隔离

| 环境 | 数据库名 | 用户 | 密码 |
|------|---------|------|------|
| Staging | news_app_staging | newsapp_staging | 单独密码 |
| Production | news_app | newsapp | 单独密码 |

### 2.4 Redis 隔离

| 环境 | Database | 密码 |
|------|----------|------|
| Staging | 1 | 单独密码 |
| Production | 0 | 单独密码 |

---

## 三、本地开发到生产流程

### 3.1 分支策略

```
main (生产)
  │
  ├─ 热修复 (hotfix/*)
  │
develop (开发集成分支)
  │
  ├─ 功能分支 (feature/new-function)
  ├─ 修复分支 (bugfix/fix-bug)
  └─ 发布分支 (release/v1.0)
```

### 3.2 完整工作流程

```
1. 本地开发
   └─ git checkout -b feature/new-function
   └─ 编码 + 本地测试
   └─ git commit -m "feat: add new function"

2. 推送到远程
   └─ git push origin feature/new-function
   └─ 创建 Pull Request 到 develop

3. CI 自动运行
   └─ 代码检查
   └─ 单元测试
   └─ 集成测试
   └─ 构建镜像

4. 合并到 develop
   └─ 自动部署到 Staging 环境
   └─ 运行 E2E 测试
   └─ 团队验证

5. 创建 PR 到 main
   └─ 代码审查
   └─ 获得批准

6. 合并到 main
   └─ 手动确认部署
   └─ 部署到 Production
   └─ 运行冒烟测试
   └─ 监控告警
```

### 3.3 命令示例

```bash
# 1. 创建功能分支
git checkout -b feature/user-search

# 2. 开发并提交
git add .
git commit -m "feat: add user search functionality"

# 3. 推送并创建 PR
git push origin feature/user-search
# 在 GitHub 上创建 PR: feature/user-search → develop

# 4. 合并后自动部署到 Staging

# 5. Staging 验证通过后，创建 PR: develop → main

# 6. 手动确认部署到 Production
```

---

## 四、详细配置

### 4.1 目录结构

```
.
├── .github/
│   └── workflows/
│       ├── ci.yml                    # 持续集成（所有分支）
│       ├── deploy-staging.yml        # 部署到 Staging（develop 分支）
│       └── deploy-production.yml     # 部署到 Production（main 分支）
│
├── deploy/
│   ├── staging/
│   │   ├── docker-compose.yml       # Staging 环境编排
│   │   ├── .env                     # Staging 环境变量
│   │   └── nginx/
│   │       └── conf.d/
│   │           └── default.conf     # Staging Nginx 配置
│   │
│   └── production/
│       ├── docker-compose.yml       # Production 环境编排
│       ├── .env                     # Production 环境变量
│       └── nginx/
│           └── conf.d/
│               └── default.conf     # Production Nginx 配置
│
├── scripts/
│   ├── deploy-to-staging.sh         # 部署到 Staging
│   ├── deploy-to-production.sh      # 部署到 Production
│   ├── rollback-staging.sh          # 回滚 Staging
│   ├── rollback-production.sh       # 回滚 Production
│   └── health-check.sh               # 健康检查脚本
│
└── backend/src/main/resources/
    ├── application-staging.properties    # Staging 配置
    └── application-production.properties # Production 配置
```

### 4.2 环境配置文件

#### Staging 环境变量 (`deploy/staging/.env`)

```bash
# ============================================================
# Staging 环境配置
# ============================================================

# 环境标识
ENVIRONMENT=staging
COMPOSE_PROJECT_NAME=news-staging

# ============================================================
# MySQL 配置
# ============================================================
MYSQL_ROOT_PASSWORD=Staging@RootPass2026
MYSQL_DATABASE=news_app_staging
MYSQL_USER=newsapp_staging
MYSQL_PASSWORD=Staging@DBPass2026
MYSQL_PORT=3306

# ============================================================
# Redis 配置
# ============================================================
REDIS_PASSWORD=Staging@RedisPass2026
REDIS_PORT=6379
REDIS_DB=1

# ============================================================
# 后端配置
# ============================================================
BACKEND_PORT=8081
SPRING_PROFILES_ACTIVE=staging
JAVA_OPTS=-Xms256m -Xmx512m

# ============================================================
# NewsAPI 配置
# ============================================================
NEWSAPI_API_KEY=your_staging_api_key

# ============================================================
# Nginx 配置
# ============================================================
NGINX_PORT=80
NGINX_SSL_PORT=443
SERVER_NAME=staging.yourdomain.com
```

#### Production 环境变量 (`deploy/production/.env`)

```bash
# ============================================================
# Production 环境配置
# ============================================================

# 环境标识
ENVIRONMENT=production
COMPOSE_PROJECT_NAME=news-production

# ============================================================
# MySQL 配置
# ============================================================
MYSQL_ROOT_PASSWORD=Prod@RootPass2026!Secure
MYSQL_DATABASE=news_app
MYSQL_USER=newsapp
MYSQL_PASSWORD=Prod@DBPass2026!Secure
MYSQL_PORT=3306

# ============================================================
# Redis 配置
# ============================================================
REDIS_PASSWORD=Prod@RedisPass2026!Secure
REDIS_PORT=6379
REDIS_DB=0

# ============================================================
# 后端配置
# ============================================================
BACKEND_PORT=8081
SPRING_PROFILES_ACTIVE=production
JAVA_OPTS=-Xms512m -Xmx1024m

# ============================================================
# NewsAPI 配置
# ============================================================
NEWSAPI_API_KEY=your_production_api_key

# ============================================================
# Nginx 配置
# ============================================================
NGINX_PORT=80
NGINX_SSL_PORT=443
SERVER_NAME=www.yourdomain.com

# ============================================================
# 监控和告警
# ============================================================
ENABLE_MONITORING=true
SLACK_WEBHOOK_URL=https://hooks.slack.com/...
EMAIL_ALERT=admin@yourdomain.com
```

### 4.3 Spring Boot 配置

#### Staging 配置 (`application-staging.properties`)

```properties
# ============================================================
# Staging 环境配置
# ============================================================

server.port=8081
spring.application.name=news-subscription-app-staging

# 数据库配置
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Redis 配置
spring.data.redis.host=${SPRING_DATA_REDIS_HOST:redis}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT:6379}
spring.data.redis.password=${SPRING_DATA_REDIS_PASSWORD}
spring.data.redis.database=${REDIS_DB:1}

# Cache 配置
spring.cache.type=redis
spring.cache.redis.time-to-live=300000

# NewsAPI 配置
newsapi.api-key=${NEWSAPI_API_KEY}

# 日志配置（详细）
logging.level.com.newsapp=DEBUG
logging.level.org.springframework.web=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [STAGING] %logger{36} - %msg%n
```

#### Production 配置 (`application-production.properties`)

```properties
# ============================================================
# Production 环境配置
# ============================================================

server.port=8081
spring.application.name=news-subscription-app

# 数据库配置
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA 配置
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Redis 配置
spring.data.redis.host=${SPRING_DATA_REDIS_HOST:redis}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT:6379}
spring.data.redis.password=${SPRING_DATA_REDIS_PASSWORD}
spring.data.redis.database=${REDIS_DB:0}

# Cache 配置
spring.cache.type=redis
spring.cache.redis.time-to-live=600000

# NewsAPI 配置
newsapi.api-key=${NEWSAPI_API_KEY}

# 日志配置（精简）
logging.level.com.newsapp=INFO
logging.level.org.springframework.web=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [PROD] %logger{36} - %msg%n
logging.file.name=/var/log/news-app/application.log
logging.logrotate.max-size=100MB
logging.logrotate.max-history=30
```

### 4.4 Docker Compose 配置

#### Staging (`deploy/staging/docker-compose.yml`)

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: ${COMPOSE_PROJECT_NAME}-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    ports:
      - "${MYSQL_PORT}:3306"
    volumes:
      - mysql_staging_data:/var/lib/mysql
      - ../../database/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    networks:
      - news-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: ${COMPOSE_PROJECT_NAME}-redis
    restart: unless-stopped
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD}
      --maxmemory 128mb
      --maxmemory-policy allkeys-lru
    ports:
      - "${REDIS_PORT}:6379"
    volumes:
      - redis_staging_data:/data
    networks:
      - news-network
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      retries: 5

  backend:
    image: news-app:${ENVIRONMENT}-${GIT_COMMIT:-latest}
    container_name: ${COMPOSE_PROJECT_NAME}-backend
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      SPRING_DATA_REDIS_PASSWORD: ${REDIS_PASSWORD}
      NEWSAPI_API_KEY: ${NEWSAPI_API_KEY}
      JAVA_OPTS: ${JAVA_OPTS}
    ports:
      - "${BACKEND_PORT}:8081"
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - news-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 60s

  nginx:
    image: nginx:1.25-alpine
    container_name: ${COMPOSE_PROJECT_NAME}-nginx
    restart: unless-stopped
    ports:
      - "${NGINX_PORT}:80"
    volumes:
      - ../../nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ../../frontend/public:/usr/share/nginx/html:ro
      - ./nginx/logs:/var/log/nginx
    depends_on:
      - backend
    networks:
      - news-network

networks:
  news-network:
    driver: bridge

volumes:
  mysql_staging_data:
    driver: local
  redis_staging_data:
    driver: local
```

#### Production (`deploy/production/docker-compose.yml`)

结构相同，但使用 `production` 标识的卷和网络。

---

## 五、部署脚本

### 5.1 部署到 Staging

```bash
#!/bin/bash
# scripts/deploy-to-staging.sh

set -e

ENVIRONMENT="staging"
VPS_HOST="${STAGING_VPS_HOST:-staging.yourdomain.com}"
VPS_USER="${STAGING_VPS_USER:-root}"
PROJECT_DIR="/var/www/news-app-staging"

echo "=========================================="
echo "  部署到 Staging 环境"
echo "=========================================="
echo "VPS: $VPS_HOST"
echo "时间: $(date)"
echo ""

# 1. 构建镜像
echo "[1/5] 构建 Docker 镜像..."
GIT_COMMIT=$(git rev-parse --short HEAD)
docker build -t news-app:staging-${GIT_COMMIT} .

# 2. 保存镜像
echo "[2/5] 保存 Docker 镜像..."
docker save news-app:staging-${GIT_COMMIT} | gzip > /tmp/news-app-staging.tar.gz

# 3. 上传到 VPS
echo "[3/5] 上传到 VPS..."
scp /tmp/news-app-staging.tar.gz ${VPS_USER}@${VPS_HOST}:${PROJECT_DIR}/
scp deploy/staging/docker-compose.yml ${VPS_USER}@${VPS_HOST}:${PROJECT_DIR}/
scp deploy/staging/.env ${VPS_USER}@${VPS_HOST}:${PROJECT_DIR}/

# 4. 在 VPS 上部署
echo "[4/5] 在 VPS 上部署..."
ssh ${VPS_USER}@${VPS_HOST} << EOF
cd ${PROJECT_DIR}

# 加载新镜像
docker load < /tmp/news-app-staging.tar.gz

# 更新 .env 中的 GIT_COMMIT
sed -i "s/GIT_COMMIT=.*/GIT_COMMIT=${GIT_COMMIT}/" .env

# 停止旧容器
docker-compose down

# 启动新容器
docker-compose up -d

# 等待健康检查
echo "等待服务启动..."
sleep 30

# 清理旧镜像
docker image prune -f

# 检查服务状态
docker-compose ps
docker-compose logs --tail=20 backend
EOF

# 5. 健康检查
echo "[5/5] 健康检查..."
./scripts/health-check.sh ${VPS_HOST} staging

echo ""
echo "=========================================="
echo "  Staging 环境部署完成!"
echo "=========================================="
echo "访问地址: http://${VPS_HOST}/"
echo ""
```

### 5.2 部署到 Production（带蓝绿部署）

```bash
#!/bin/bash
# scripts/deploy-to-production.sh

set -e

ENVIRONMENT="production"
VPS_HOST="${PRODUCTION_VPS_HOST:-47.103.204.114}"
VPS_USER="${PRODUCTION_VPS_USER:-root}"
PROJECT_DIR="/var/www/news-app"
BLUE_PORT=8081
GREEN_PORT=8082

echo "=========================================="
echo "  部署到 Production 环境 (蓝绿部署)"
echo "=========================================="
echo "VPS: $VPS_HOST"
echo "时间: $(date)"
echo ""

# 确认部署
read -p "确认部署到生产环境? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "部署已取消"
    exit 0
fi

# 1. 构建镜像
echo "[1/7] 构建 Docker 镜像..."
GIT_COMMIT=$(git rev-parse --short HEAD)
docker build -t news-app:production-${GIT_COMMIT} .

# 2. 保存镜像
echo "[2/7] 保存 Docker 镜像..."
docker save news-app:production-${GIT_COMMIT} | gzip > /tmp/news-app-production.tar.gz

# 3. 上传到 VPS
echo "[3/7] 上传到 VPS..."
scp /tmp/news-app-production.tar.gz ${VPS_USER}@${VPS_HOST}:${PROJECT_DIR}/
scp deploy/production/docker-compose.yml ${VPS_USER}@${VPS_HOST}:${PROJECT_DIR}/
scp deploy/production/.env ${VPS_USER}@${VPS_HOST}:${PROJECT_DIR}/

# 4. 在 VPS 上部署
echo "[4/7] 在 VPS 上执行蓝绿部署..."
ssh ${VPS_USER}@${VPS_HOST} << 'EOF'
PROJECT_DIR="/var/www/news-app"
cd ${PROJECT_DIR}

# 加载新镜像
docker load < /tmp/news-app-production.tar.gz

# 检查当前活跃环境
if docker ps | grep -q "news-backend-blue"; then
    CURRENT="blue"
    NEW="green"
    NEW_PORT=8082
else
    CURRENT="green"
    NEW="blue"
    NEW_PORT=8081
fi

echo "当前环境: $CURRENT"
echo "新环境: $NEW (端口 $NEW_PORT)"

# 启动新环境
export BACKEND_PORT=$NEW_PORT
export COMPOSE_PROJECT_NAME=news-production-${NEW}
docker-compose up -d

# 等待新环境健康检查
echo "等待新环境启动..."
for i in {1..30}; do
    if curl -f http://localhost:${NEW_PORT}/actuator/health >/dev/null 2>&1; then
        echo "新环境健康检查通过!"
        break
    fi
    echo "等待中... ($i/30)"
    sleep 2
done

# 运行冒烟测试
echo "运行冒烟测试..."
curl -f http://localhost:${NEW_PORT}/api/auth/current || {
    echo "冒烟测试失败!"
    docker-compose down
    exit 1
}

# 切换 Nginx 到新环境
echo "切换 Nginx 到新环境..."
sed -i "s/proxy_pass http:\/\/backend:.*/proxy_pass http:\/\/backend-${NEW}:${NEW_PORT};/" /etc/nginx/conf.d/news-app.conf
nginx -s reload

echo "等待 10 秒确认稳定..."
sleep 10

# 检查新环境
if curl -f http://localhost/health >/dev/null 2>&1; then
    echo "新环境运行正常!"

    # 停止旧环境
    echo "停止旧环境 ($CURRENT)..."
    export COMPOSE_PROJECT_NAME=news-production-${CURRENT}
    docker-compose down

    echo "蓝绿部署完成!"
else
    echo "新环境检查失败，回滚!"
    sed -i "s/proxy_pass http:\/\/backend-.*/proxy_pass http:\/\/backend-${CURRENT}:${NEW_PORT};/" /etc/nginx/conf.d/news-app.conf
    nginx -s reload
    docker-compose -f docker-compose.yml -p news-production-${NEW} down
    exit 1
fi

# 清理旧镜像
docker image prune -f
EOF

# 5. 健康检查
echo "[5/7] 健康检查..."
./scripts/health-check.sh ${VPS_HOST} production

# 6. 运行 E2E 测试
echo "[6/7] 运行 E2E 测试..."
cd frontend
npm run test:e2e:production
cd ..

# 7. 发送通知
echo "[7/7] 发送部署通知..."
# ./scripts/notify-deployment.sh success production

echo ""
echo "=========================================="
echo "  Production 环境部署完成!"
echo "=========================================="
echo "访问地址: http://${VPS_HOST}/"
echo ""
```

### 5.3 健康检查脚本

```bash
#!/bin/bash
# scripts/health-check.sh

HOST=$1
ENVIRONMENT=$2
MAX_RETRIES=30
RETRY_INTERVAL=2

echo "=========================================="
echo "  健康检查 - $ENVIRONMENT 环境"
echo "=========================================="
echo "主机: $HOST"
echo ""

for i in $(seq 1 $MAX_RETRIES); do
    echo "检查中... ($i/$MAX_RETRIES)"

    # 检查 HTTP 状态
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://${HOST}/health)
    if [ "$HTTP_CODE" = "200" ]; then
        echo "✅ HTTP 健康检查通过"

        # 检查 API 端点
        API_HEALTH=$(curl -s http://${HOST}/api/actuator/health)
        if echo "$API_HEALTH" | grep -q '"status":"UP"'; then
            echo "✅ API 健康检查通过"

            # 检查数据库连接
            DB_CHECK=$(curl -s http://${HOST}/api/actuator/health/db)
            if echo "$DB_CHECK" | grep -q '"status":"UP"'; then
                echo "✅ 数据库连接正常"

                # 检查 Redis 连接
                REDIS_CHECK=$(curl -s http://${HOST}/api/actuator/health/redis)
                if echo "$REDIS_CHECK" | grep -q '"status":"UP"'; then
                    echo "✅ Redis 连接正常"

                    echo ""
                    echo "=========================================="
                    echo "  所有健康检查通过!"
                    echo "=========================================="
                    exit 0
                fi
            fi
        fi
    fi

    if [ $i -lt $MAX_RETRIES ]; then
        sleep $RETRY_INTERVAL
    fi
done

echo ""
echo "=========================================="
echo "  健康检查失败!"
echo "=========================================="
exit 1
```

---

## 六、CI/CD 流水线

### 6.1 持续集成 (`.github/workflows/ci.yml`)

```yaml
name: 持续集成

on:
  push:
    branches: [ main, develop, feature/*, hotfix/* ]
  pull_request:
    branches: [ main, develop ]

jobs:
  # 代码质量检查
  code-quality:
    name: 代码质量检查
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: 后端代码检查
        run: |
          cd backend
          mvn checkstyle:check
          mvn pmd:check

      - name: 前端代码检查
        run: |
          cd frontend
          npm run lint

  # 单元测试
  unit-tests:
    name: 单元测试
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: 后端单元测试
        run: |
          cd backend
          mvn test -DskipITs=true

      - name: 前端单元测试
        run: |
          cd frontend
          npm ci
          npm run test

  # 集成测试
  integration-tests:
    name: 集成测试
    runs-on: ubuntu-latest
    needs: unit-tests
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test123
          MYSQL_DATABASE: news_app_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
    steps:
      - uses: actions/checkout@v4

      - name: 后端集成测试
        run: |
          cd backend
          mvn verify -DskipUnitTests=true
        env:
          SPRING_PROFILES_ACTIVE: test

  # 构建镜像
  build:
    name: 构建 Docker 镜像
    runs-on: ubuntu-latest
    needs: [code-quality, unit-tests, integration-tests]
    steps:
      - uses: actions/checkout@v4

      - name: 构建 Docker 镜像
        run: |
          docker build -t news-app:${{ github.sha }} .

      - name: 保存镜像
        run: |
          docker save news-app:${{ github.sha }} | gzip > /tmp/news-app.tar.gz

      - name: 上传镜像制品
        uses: actions/upload-artifact@v4
        with:
          name: docker-image
          path: /tmp/news-app.tar.gz
          retention-days: 7
```

### 6.2 部署到 Staging (`.github/workflows/deploy-staging.yml`)

```yaml
name: 部署到 Staging

on:
  push:
    branches: [ develop ]

jobs:
  deploy:
    name: 部署到 Staging
    runs-on: ubuntu-latest
    environment:
      name: staging
      url: http://staging.yourdomain.com
    steps:
      - uses: actions/checkout@v4

      - name: 下载镜像制品
        uses: actions/download-artifact@v4
        with:
          name: docker-image
          path: /tmp

      - name: 加载镜像
        run: |
          docker load < /tmp/news-app.tar.gz
          docker tag news-app:${{ github.sha }} news-app:staging-latest

      - name: 上传到 Staging VPS
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.STAGING_VPS_HOST }}
          username: ${{ secrets.STAGING_VPS_USER }}
          key: ${{ secrets.STAGING_VPS_SSH_KEY }}
          script: |
            cd /var/www/news-app-staging
            docker-compose pull
            docker-compose up -d
            docker-compose exec -T backend ./scripts/wait-for-health.sh

      - name: 健康检查
        run: |
          ./scripts/health-check.sh ${{ secrets.STAGING_VPS_HOST }} staging

      - name: 运行 E2E 测试
        run: |
          cd frontend
          npm run test:e2e:staging

      - name: 通知团队
        if: always()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          text: |
            Staging 环境部署 ${{ job.status }}
            分支: ${{ github.ref }}
            提交: ${{ github.sha }}
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

### 6.3 部署到 Production (`.github/workflows/deploy-production.yml`)

```yaml
name: 部署到 Production

on:
  push:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      confirm:
        description: '确认部署到生产环境? (输入 yes 继续)'
        required: true
        default: 'no'

jobs:
  deploy:
    name: 部署到 Production
    runs-on: ubuntu-latest
    environment:
      name: production
      url: http://www.yourdomain.com
    steps:
      - uses: actions/checkout@v4

      - name: 确认部署
        if: github.event_name == 'workflow_dispatch'
        run: |
          CONFIRM="${{ github.event.inputs.confirm }}"
          if [ "$CONFIRM" != "yes" ]; then
            echo "部署已取消"
            exit 1
          fi

      - name: 下载镜像制品
        uses: actions/download-artifact@v4
        with:
          name: docker-image
          path: /tmp

      - name: 加载镜像
        run: |
          docker load < /tmp/news-app.tar.gz
          docker tag news-app:${{ github.sha }} news-app:production-latest

      - name: 上传到 Production VPS
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.PRODUCTION_VPS_HOST }}
          username: ${{ secrets.PRODUCTION_VPS_USER }}
          key: ${{ secrets.PRODUCTION_VPS_SSH_KEY }}
          source: "/tmp/news-app.tar.gz"
          target: "/var/www/news-app/"

      - name: 蓝绿部署
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.PRODUCTION_VPS_HOST }}
          username: ${{ secrets.PRODUCTION_VPS_USER }}
          key: ${{ secrets.PRODUCTION_VPS_SSH_KEY }}
          script: |
            cd /var/www/news-app
            ./scripts/blue-green-deploy.sh

      - name: 健康检查
        run: |
          ./scripts/health-check.sh ${{ secrets.PRODUCTION_VPS_HOST }} production

      - name: 冒烟测试
        run: |
          ./scripts/smoke-test.sh ${{ secrets.PRODUCTION_VPS_HOST }}

      - name: 通知团队
        if: always()
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          text: |
            Production 环境部署 ${{ job.status }}
            分支: ${{ github.ref }}
            提交: ${{ github.sha }}
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}

      - name: 创建部署记录
        if: success()
        run: |
          echo "部署时间: $(date)" >> deploy-log.txt
          echo "提交: ${{ github.sha }}" >> deploy-log.txt
```

---

## 七、回滚策略

### 7.1 快速回滚脚本

```bash
#!/bin/bash
# scripts/rollback-production.sh

ENVIRONMENT=$1
VPS_HOST="${PRODUCTION_VPS_HOST:-47.103.204.114}"
VPS_USER="${PRODUCTION_VPS_USER:-root}"

echo "=========================================="
echo "  回滚 Production 环境"
echo "=========================================="

# 获取要回滚到的版本
echo "可用的版本:"
ssh ${VPS_USER}@${VPS_HOST} 'docker images | grep news-app | grep production'

read -p "输入要回滚到的镜像标签: " IMAGE_TAG

# 确认回滚
read -p "确认回滚到 ${IMAGE_TAG}? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    echo "回滚已取消"
    exit 0
fi

# 执行回滚
ssh ${VPS_USER}@${VPS_HOST} << EOF
cd /var/www/news-app

# 停止当前环境
docker-compose down

# 启动指定版本
docker tag news-app:${IMAGE_TAG} news-app:production-current
docker-compose up -d

# 等待健康检查
sleep 30

# 验证
curl -f http://localhost:8081/actuator/health || {
    echo "回滚后健康检查失败!"
    exit 1
}

echo "回滚成功!"
EOF

echo "回滚完成"
```

### 7.2 一键回滚到上一个版本

```bash
#!/bin/bash
# scripts/rollback-to-previous.sh

ssh root@47.103.204.114 << 'EOF'
cd /var/www/news-app

# 获取上一个运行的镜像
PREVIOUS_IMAGE=$(docker ps --filter "name=news-backend" --format "{{.Image}}" | head -1 | sed 's/news-app://' | sed 's/-current//')

if [ -z "$PREVIOUS_IMAGE" ]; then
    echo "无法找到上一个版本"
    exit 1
fi

echo "回滚到: $PREVIOUS_IMAGE"

# 更新 docker-compose.yml 中的镜像标签
sed -i "s/image: news-app:.*/image: news-app:${PREVIOUS_IMAGE}/" docker-compose.yml

# 重启服务
docker-compose down
docker-compose up -d

echo "回滚完成"
EOF
```

---

## 八、监控和日志

### 8.1 日志聚合

```bash
# 查看所有环境日志
./scripts/logs.sh staging
./scripts/logs.sh production

# 日志脚本内容
#!/bin/bash
# scripts/logs.sh

ENVIRONMENT=$1
VPS_HOST=$2

case $ENVIRONMENT in
    staging)
        ssh root@${VPS_HOST} 'cd /var/www/news-app-staging && docker-compose logs -f --tail=100'
        ;;
    production)
        ssh root@${VPS_HOST} 'cd /var/www/news-app && docker-compose logs -f --tail=100'
        ;;
esac
```

### 8.2 监控指标

| 指标 | Staging | Production | 告警阈值 |
|------|---------|------------|----------|
| CPU 使用率 | < 70% | < 80% | > 90% |
| 内存使用率 | < 70% | < 80% | > 90% |
| 磁盘使用率 | < 80% | < 80% | > 90% |
| API 响应时间 | < 500ms | < 200ms | > 1s |
| 错误率 | < 5% | < 1% | > 5% |
| 可用性 | > 95% | > 99.9% | < 99% |

---

## 快速参考

### 环境信息

| 环境 | VPS | 域名 | 部署分支 |
|------|-----|------|---------|
| Staging | 待分配 | staging.yourdomain.com | develop |
| Production | 47.103.204.114 | www.yourdomain.com | main |

### 部署命令

```bash
# 部署到 Staging（自动）
git push origin develop

# 部署到 Production（手动确认）
git push origin main
# 然后在 GitHub Actions 中手动确认

# 本地手动部署
./scripts/deploy-to-staging.sh
./scripts/deploy-to-production.sh

# 回滚
./scripts/rollback-production.sh [IMAGE_TAG]
```

### 访问地址

| 环境 | URL |
|------|-----|
| Staging 前端 | http://47.103.204.114/ |
| Staging API | http://47.103.204.114/api/ |
| Production 前端 | http://139.224.189.183/ |
| Production API | http://139.224.189.183/api/ |

---

**状态**: 📝 方案已完成，待实施
**最后更新**: 2026-01-02
