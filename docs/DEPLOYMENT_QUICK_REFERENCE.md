# 双环境部署快速参考

**完整指南**: 参见 `docs/DUAL_ENVIRONMENT_DEPLOYMENT_GUIDE.md`

---

## 环境配置

| 环境 | VPS | 域名 | 部署分支 | 部署方式 |
|------|-----|------|---------|---------|
| **Staging** | 47.103.204.114 | staging.yourdomain.com | develop | 自动部署 |
| **Production** | 139.224.189.183 | www.yourdomain.com | main | 蓝绿部署（手动确认）|

---

## 快速部署命令

### 本地手动部署

```bash
# 部署到 Staging
./scripts/deploy-to-staging.sh

# 部署到 Production（需要手动确认）
./scripts/deploy-to-production.sh

# 回滚 Production
./scripts/rollback-production.sh [image_tag]

# 健康检查
./scripts/health-check.sh <host> <environment>
```

### Git 推送自动部署

```bash
# 推送到 develop → 自动部署到 Staging
git checkout develop
git merge feature/new-feature
git push origin develop

# 推送到 main → 需要在 GitHub Actions 手动确认部署到 Production
git checkout main
git merge develop
git push origin main
# 然后在 GitHub → Actions → Deploy to Production → 点击 "Run workflow"
```

---

## GitHub Secrets 配置

在 GitHub 仓库设置中添加以下 Secrets：

### Staging 环境

| Secret | 说明 | 示例 |
|--------|------|------|
| `STAGING_VPS_HOST` | Staging VPS 主机 | 47.103.204.114 |
| `STAGING_VPS_USER` | SSH 用户名 | root |
| `STAGING_VPS_SSH_KEY` | SSH 私钥 | -----BEGIN RSA... |

### Production 环境

| Secret | 说明 | 示例 |
|--------|------|------|
| `PRODUCTION_VPS_HOST` | Production VPS 主机 | 139.224.189.183 |
| `PRODUCTION_VPS_USER` | SSH 用户名 | root |
| `PRODUCTION_VPS_SSH_KEY` | SSH 私钥 | -----BEGIN RSA... |

### 通知（可选）

| Secret | 说明 |
|--------|------|
| `SLACK_WEBHOOK_URL` | Slack Webhook URL 用于部署通知 |

---

## 目录结构

```
.
├── deploy/
│   ├── staging/
│   │   ├── docker-compose.yml       # Staging 环境编排
│   │   ├── .env                     # Staging 环境变量
│   │   └── nginx/conf.d/
│   │       └── default.conf         # Staging Nginx 配置
│   │
│   └── production/
│       ├── docker-compose.yml       # Production 环境编排（蓝绿）
│       ├── .env                     # Production 环境变量
│       └── nginx/conf.d/
│           └── default.conf         # Production Nginx 配置
│
├── scripts/
│   ├── deploy-to-staging.sh         # 部署到 Staging ✅ 可执行
│   ├── deploy-to-production.sh      # 部署到 Production ✅ 可执行
│   ├── health-check.sh              # 健康检查 ✅ 可执行
│   └── rollback-production.sh       # 回滚 Production ✅ 可执行
│
├── .github/workflows/
│   ├── deploy-staging.yml           # Staging 自动部署
│   └── deploy-production.yml        # Production 手动部署
│
└── backend/src/main/resources/
    ├── application-staging.properties    # Staging 配置
    └── application-production.properties # Production 配置
```

---

## 访问地址

### Staging 环境

| 服务 | URL |
|------|-----|
| 前端 | http://47.103.204.114/ |
| 注册 | http://47.103.204.114/register.html |
| 登录 | http://47.103.204.114/login.html |
| API | http://47.103.204.114/api/ |
| 健康检查 | http://47.103.204.114/health |

### Production 环境

| 服务 | URL |
|------|-----|
| 前端 | http://139.224.189.183/ |
| 注册 | http://139.224.189.183/register.html |
| 登录 | http://139.224.189.183/login.html |
| API | http://139.224.189.183/api/ |
| 健康检查 | http://139.224.189.183/health |

---

## 典型工作流程

### 1. 功能开发

```bash
# 创建功能分支
git checkout -b feature/new-function

# 开发并提交
git add .
git commit -m "feat: add new function"

# 推送并创建 PR
git push origin feature/new-function
# 在 GitHub 创建 PR: feature/new-function → develop
```

### 2. 部署到 Staging

```bash
# PR 合并到 develop 后自动部署
# 或手动触发:
./scripts/deploy-to-staging.sh

# 验证 Staging 环境
curl http://staging.yourdomain.com/health
```

### 3. 部署到 Production

```bash
# Staging 验证通过后，创建 PR: develop → main
# PR 审批合并后，手动触发部署:
# GitHub → Actions → Deploy to Production → Run workflow → 输入 yes

# 或本地手动部署（谨慎使用）
./scripts/deploy-to-production.sh
```

### 4. 回滚（如需要）

```bash
# 查看可用版本
ssh root@47.103.204.114 'docker images | grep news-app | grep production'

# 回滚到指定版本
./scripts/rollback-production.sh production-abc1234
```

---

## 监控和日志

### 查看日志

```bash
# Staging 日志
ssh root@47.103.204.114 'cd /var/www/news-app-staging && docker-compose logs -f'

# Production 日志
ssh root@139.224.189.183 'cd /var/www/news-app && docker-compose logs -f'

# 查看特定服务
ssh root@139.224.189.183 'docker logs -f news-production-backend-blue'
```

### 服务状态

```bash
# Staging
ssh root@47.103.204.114 'cd /var/www/news-app-staging && docker-compose ps'

# Production
ssh root@139.224.189.183 'cd /var/www/news-app && docker-compose ps'
```

### 资源使用

```bash
# CPU/内存
ssh root@139.224.189.183 'docker stats'

# 磁盘使用
ssh root@139.224.189.183 'df -h'
```

---

## 蓝绿部署说明

Production 环境使用蓝绿部署策略：

```
┌─────────────────────────────────────────────────────────┐
│                  Nginx (Port 80)                        │
│                                                         │
│  ┌─────────────────────┐    ┌─────────────────────┐   │
│  │   Blue Environment  │    │  Green Environment  │   │
│  │   (Port 8081)       │    │   (Port 8082)       │   │
│  │   ┌───────────┐     │    │   ┌───────────┐     │   │
│  │   │  Backend  │     │    │   │  Backend  │     │   │
│  │   └───────────┘     │    │   └───────────┘     │   │
│  │                     │    │                     │   │
│  │   [ACTIVE]          │    │   [IDLE]            │   │
│  └─────────────────────┘    └─────────────────────┘   │
│            ↑                         ↑               │
└────────────┴─────────────────────────┴───────────────┘
             当前流量                  新版本部署

部署流程:
1. 在空闲环境(Green)启动新版本
2. 健康检查和冒烟测试
3. Nginx 切换流量 → Green
4. 验证新环境
5. 停止旧环境(Blue)
```

---

## 环境变量对照

| 变量 | Staging | Production |
|------|---------|------------|
| 数据库名 | news_app_staging | news_app |
| 数据库用户 | newsapp_staging | newsapp |
| Redis DB | 1 | 0 |
| 日志级别 | DEBUG | INFO |
| 缓存 TTL | 5分钟 | 10分钟 |
| JVM 内存 | 512MB | 1GB |

---

## 故障排查

### 部署失败

```bash
# 查看部署日志
./scripts/deploy-to-staging.sh 2>&1 | tee deployment.log

# 在 VPS 上查看
ssh root@47.103.204.114 'cd /var/www/news-app-staging && docker-compose logs backend'
```

### 服务无法启动

```bash
# 检查容器状态
ssh root@139.224.189.183 'docker ps -a'

# 检查容器日志
ssh root@139.224.189.183 'docker logs news-production-backend-blue'

# 检查端口占用
ssh root@139.224.189.183 'netstat -tlnp | grep :8081'
```

### 健康检查失败

```bash
# 手动健康检查
curl http://139.224.189.183/health
curl http://139.224.189.183/api/auth/current

# 检查 Docker 内部网络
ssh root@139.224.189.183 'docker exec news-production-backend-blue curl -f http://localhost:8081/api/auth/current'
```

---

**状态**: ✅ 双环境 CI/CD 部署方案已完成
**最后更新**: 2026-01-02
