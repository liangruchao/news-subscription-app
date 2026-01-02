# News Subscription System v1.0 - 基线发布说明

## 📅 发布信息

- **发布日期**: 2025年1月2日
- **版本号**: v1.0
- **基线分支**: main
- **Git Tag**: v1.0
- **发布类型**: Major Feature Release (Baseline)

---

## 🎯 发布概述

v1.0 是新闻订阅系统的第一个稳定基线版本，标志着项目从开发阶段进入生产就绪状态。本版本整合了 Group A 基础功能、Group B 个人中心功能以及关键的 P0 安全修复。

### 核心成果
- ✅ 完整的用户认证和订阅系统
- ✅ 丰富的个人中心功能
- ✅ 全面的国际化支持
- ✅ 安全的环境配置管理
- ✅ 完善的文档体系

---

## 📦 功能清单

### Group A: 基础功能 (Stage 2)

#### 1. 用户认证系统
| 功能 | 描述 | 状态 |
|------|------|------|
| 用户注册 | 用户名、邮箱唯一性验证，BCrypt 密码加密 | ✅ |
| 用户登录 | Session 会话管理，30分钟超时 | ✅ |
| 用户登出 | 清除 Session，跳转登录页 | ✅ |
| JWT 认证 | 可选的令牌认证支持 | ✅ |

#### 2. 新闻订阅管理
| 功能 | 描述 | 状态 |
|------|------|------|
| 类别订阅 | 支持 7 种新闻类别订阅 | ✅ |
| 防重复订阅 | 数据库唯一约束 | ✅ |
| 取消订阅 | 二次确认删除 | ✅ |
| 中文类别名 | 类别中英文映射 | ✅ |

#### 3. NewsAPI 集成
| 功能 | 描述 | 状态 |
|------|------|------|
| 实时新闻 | 调用 NewsAPI.org 获取最新新闻 | ✅ |
| Redis 缓存 | 减少API调用，提升性能 | ✅ |
| 自动刷新 | 定时任务自动更新缓存 | ✅ |
| 分类聚合 | 整合多个类别新闻 | ✅ |

**支持的新闻类别**:
- 商业 (business)
- 娱乐 (entertainment)
- 综合 (general)
- 健康 (health)
- 科学 (science)
- 体育 (sports)
- 科技 (technology)

---

### Group B: 个人中心功能 (Stage 3)

#### 1. 个人中心 (Profile)
| 功能 | 描述 | 状态 |
|------|------|------|
| 个人信息展示 | 用户名、邮箱、简介 | ✅ |
| 头像上传 | 支持图片上传，10MB 限制 | ✅ |
| 密码修改 | 原密码验证，6位最小长度 | ✅ |
| 登录历史 | 显示最近 10 次登录记录 | ✅ |
| 用户统计 | 订阅数、注册天数、消息数 | ✅ |
| 账户注销 | 二次确认，级联删除 | ✅ |

#### 2. 消息中心 (Messages)
| 功能 | 描述 | 状态 |
|------|------|------|
| 消息列表 | 分页展示所有消息 | ✅ |
| 消息筛选 | 全部/未读，按类型筛选 | ✅ |
| 标记已读 | 单条或批量标记 | ✅ |
| 清空历史 | 清除所有消息记录 | ✅ |

**消息类型**:
- 系统消息 (SYSTEM)
- 订阅消息 (SUBSCRIPTION)
- 新闻消息 (NEWS)

#### 3. 系统公告 (Announcements)
| 功能 | 描述 | 状态 |
|------|------|------|
| 公告列表 | 分页展示所有公告 | ✅ |
| 置顶标识 | 高亮重要公告 | ✅ |
| 状态筛选 | 已发布/草稿 | ✅ |
| 优先级 | 数字排序显示 | ✅ |

#### 4. 用户偏好设置 (Preferences)
| 功能 | 描述 | 状态 |
|------|------|------|
| 通知设置 | 新闻/系统/订阅通知开关 | ✅ |
| 显示设置 | 每页数量、紧凑模式 | ✅ |
| 语言设置 | 中文/English 切换 | ✅ |
| 隐私设置 | 公开资料、在线状态 | ✅ |
| 自动保存 | 500ms 防抖保存 | ✅ |

#### 5. 国际化 (i18n)
| 功能 | 描述 | 状态 |
|------|------|------|
| 语言包 | 中英文完整翻译 | ✅ |
| 即时切换 | 无需刷新页面 | ✅ |
| 偏好持久化 | 保存到 localStorage 和数据库 | ✅ |
| 自动检测 | 根据浏览器语言自动选择 | ✅ |

**支持的页面**:
- ✅ index.html (首页)
- ✅ profile.html (个人中心)
- ✅ messages.html (消息中心)
- ✅ announcements.html (系统公告)
- ✅ preferences.html (偏好设置)
- ⏳ login.html (登录页面) - 待完善
- ⏳ register.html (注册页面) - 待完善

---

## 🔒 安全修复 (P0)

### 1. 密钥安全化
| 问题 | 修复 | 状态 |
|------|------|------|
| 硬编码 NewsAPI 密钥 | 使用环境变量 `${NEWSAPI_API_KEY:#{null}}` | ✅ |
| 硬编码数据库密码 | 使用环境变量 `${DB_PASSWORD:#{null}}` | ✅ |
| .env 文件被追踪 | 更新 .gitignore，创建 .env.example | ✅ |

**影响文件**:
- `backend/src/main/resources/application.properties`
- `backend/src/main/resources/application-staging.properties`
- `deploy/staging/.env`
- `deploy/production/.env`

### 2. CI/CD 修复
| 问题 | 修复 | 状态 |
|------|------|------|
| 错误的条件判断 | `github.branch` → `github.ref` | ✅ |

### 3. 功能缺陷修复
| 问题 | 修复 | 状态 |
|------|------|------|
| User 实体缺少字段 | 添加 avatarUrl, bio | ✅ |
| UserService 方法缺失 | 添加 4 个核心方法 | ✅ |
| NewsService 缓存刷新 | 添加 preloadAllCategories, autoRefreshAllCache | ✅ |
| StatisticsService 统计错误 | count() → countByUserId() | ✅ |
| index.html 异步调用 | 添加 async/await | ✅ |
| 重复的 JS 模块 | 删除 frontend/js/ | ✅ |

---

## 🏗️ 技术架构

### 后端技术栈
```
┌─────────────────────────────────────┐
│         Controller Layer            │
│  (REST API Endpoints)               │
│  - AuthController                   │
│  - SubscriptionController           │
│  - NewsController                   │
│  - UserProfileController            │
│  - MessageController                │
│  - AnnouncementController           │
│  - UserPreferenceController         │
│  - StatisticsController             │
│  - LoginHistoryController           │
│  - AdminController                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Service Layer               │
│  (Business Logic)                   │
│  - UserService                      │
│  - SubscriptionService              │
│  - NewsService (+NewsAPI)           │
│  - UserProfileService               │
│  - MessageService                   │
│  - AnnouncementService              │
│  - UserPreferenceService            │
│  - StatisticsService                │
│  - LoginHistoryService              │
│  - FileStorageService               │
│  - CacheService (Redis)             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Repository Layer              │
│  (Data Access)                      │
│  - UserRepository                   │
│  - SubscriptionRepository           │
│  - MessageRepository                │
│  - AnnouncementRepository           │
│  - UserPreferenceRepository         │
│  - LoginHistoryRepository           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Database Layer               │
│  MySQL 9.x + JPA/Hibernate          │
│  - users                            │
│  - subscriptions                    │
│  - messages                         │
│  - announcements                    │
│  - user_preferences                 │
│  - login_history                    │
└─────────────────────────────────────┘
```

### 前端架构
```
┌─────────────────────────────────────┐
│         HTML Pages                  │
│  - index.html (首页)                │
│  - login.html (登录)                │
│  - register.html (注册)             │
│  - profile.html (个人中心)          │
│  - messages.html (消息中心)         │
│  - announcements.html (公告)        │
│  - preferences.html (偏好设置)      │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      JavaScript Modules             │
│  - api.js (HTTP 客户端)             │
│  - auth.js (认证模块)               │
│  - news.js (订阅和新闻)             │
│  - profile.js (个人中心)            │
│  - messages.js (消息中心)           │
│  - announcements.js (公告)          │
│  - preferences.js (偏好设置)        │
│  - i18n.js (国际化)                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         REST API                    │
│  /api/auth/*                        │
│  /api/subscriptions/*               │
│  /api/news/*                        │
│  /api/user/*                        │
│  /api/messages/*                    │
│  /api/announcements/*               │
│  /api/preferences/*                 │
│  /api/statistics/*                  │
└─────────────────────────────────────┘
```

### 技术栈详情
| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 3.2.0 |
| **Java 版本** | Java | 21 |
| **数据库** | MySQL | 9.x |
| **缓存** | Redis | 7.x |
| **ORM** | Spring Data JPA | - |
| **安全** | Spring Security + BCrypt | - |
| **前端** | Vanilla JavaScript | ES6+ |
| **构建工具** | Maven | 3.9+ |
| **容器化** | Docker + Docker Compose | - |
| **反向代理** | Nginx | 1.25+ |
| **CI/CD** | GitHub Actions | - |

---

## 📊 数据库设计

### 核心表结构

#### users (用户表)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    bio VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### subscriptions (订阅表)
```sql
CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_subscription (user_id, category),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### messages (消息表)
```sql
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### announcements (公告表)
```sql
CREATE TABLE announcements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    priority INT DEFAULT 0,
    is_pinned BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### user_preferences (用户偏好表)
```sql
CREATE TABLE user_preferences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    news_notification_enabled BOOLEAN DEFAULT TRUE,
    system_notification_enabled BOOLEAN DEFAULT TRUE,
    subscription_notification_enabled BOOLEAN DEFAULT TRUE,
    news_page_size INT DEFAULT 20,
    compact_mode BOOLEAN DEFAULT FALSE,
    language VARCHAR(10) DEFAULT 'zh-CN',
    public_profile BOOLEAN DEFAULT TRUE,
    show_online_status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

#### login_history (登录历史表)
```sql
CREATE TABLE login_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 📁 项目结构

### 后端目录结构
```
backend/
├── src/main/java/com/newsapp/
│   ├── config/           # 配置类
│   │   ├── SecurityConfig.java
│   │   └── FileStorageConfig.java
│   ├── controller/       # REST 控制器
│   │   ├── AuthController.java
│   │   ├── SubscriptionController.java
│   │   ├── NewsController.java
│   │   ├── UserProfileController.java
│   │   ├── MessageController.java
│   │   ├── AnnouncementController.java
│   │   ├── UserPreferenceController.java
│   │   ├── StatisticsController.java
│   │   ├── LoginHistoryController.java
│   │   └── AdminController.java
│   ├── dto/              # 数据传输对象
│   ├── entity/           # JPA 实体
│   ├── repository/       # 数据访问层
│   ├── service/          # 业务逻辑层
│   ├── exception/        # 异常处理
│   └── util/             # 工具类
├── src/main/resources/
│   ├── application.properties
│   ├── application-staging.properties
│   └── application-production.properties
└── src/test/java/        # 单元测试
```

### 前端目录结构
```
frontend/public/
├── index.html            # 首页
├── login.html            # 登录页
├── register.html         # 注册页
├── profile.html          # 个人中心
├── messages.html         # 消息中心
├── announcements.html    # 系统公告
├── preferences.html      # 偏好设置
├── css/                  # 样式文件
│   ├── style.css
│   ├── profile.css
│   ├── messages.css
│   ├── announcements.css
│   └── preferences.css
└── js/                   # JavaScript 模块
    ├── api.js
    ├── auth.js
    ├── news.js
    ├── profile.js
    ├── messages.js
    ├── announcements.js
    ├── preferences.js
    └── i18n.js
```

### 部署目录结构
```
deploy/
├── staging/              # 测试环境
│   ├── .env.example      # 环境变量模板
│   ├── docker-compose.yml
│   └── nginx.conf
├── production/           # 生产环境
│   ├── .env.example
│   ├── docker-compose.yml
│   └── nginx.conf
└── scripts/              # 部署脚本
```

---

## 🚀 部署指南

### 环境变量配置

创建 `.env` 文件（参考 `.env.example`）：

```bash
# NewsAPI 配置
NEWSAPI_API_KEY=your_newsapi_key_here

# 数据库配置
DB_PASSWORD=your_secure_password_here

# Redis 配置
REDIS_PASSWORD=your_redis_password_here
```

### 本地开发启动

**后端**:
```bash
cd backend
mvn spring-boot:run
```

**前端**:
```bash
cd frontend
npm install
npm run dev
```

**数据库**:
```bash
mysql -u root < database/init.sql
```

### Docker 部署

**Staging 环境**:
```bash
cd deploy/staging
cp .env.example .env
# 编辑 .env 文件
docker-compose up -d
```

**Production 环境**:
```bash
cd deploy/production
cp .env.example .env
# 编辑 .env 文件
docker-compose up -d
```

### 端口说明
| 服务 | 端口 |
|------|------|
| 前端 | 8080 |
| 后端 | 8081 |
| MySQL | 3306 |
| Redis | 6379 |
| Nginx | 80/443 |

---

## 📝 API 文档

### 认证相关
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/current` | GET | 获取当前用户 |

### 订阅管理
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/subscriptions` | GET | 获取订阅列表 |
| `/api/subscriptions` | POST | 添加订阅 |
| `/api/subscriptions/{category}` | DELETE | 取消订阅 |

### 新闻获取
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/news` | GET | 获取用户订阅的新闻 |
| `/api/news/category/{category}` | GET | 获取指定类别新闻 |

### 个人中心
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/user/profile` | GET | 获取用户资料 |
| `/api/user/profile` | PUT | 更新用户资料 |
| `/api/user/password` | POST | 修改密码 |
| `/api/user/avatar` | POST | 上传头像 |
| `/api/user/stats` | GET | 获取用户统计 |
| `/api/user/login-history` | GET | 获取登录历史 |
| `/api/user/delete-account` | DELETE | 注销账户 |

### 消息中心
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/messages` | GET | 获取消息列表 |
| `/api/messages/{id}` | PUT | 标记消息已读 |
| `/api/messages/read-all` | POST | 全部标记已读 |
| `/api/messages/clear` | DELETE | 清空消息历史 |
| `/api/messages/unread-count` | GET | 获取未读数量 |

---

## ⚠️ 已知限制

### 功能限制
1. **NewsAPI 配额**: 免费版限制 100 次/天
2. **Session 认证**: 不支持分布式部署（需要 Session 共享）
3. **测试覆盖率**: 当前仅 11%，需要提升到 60%+

### 待完善功能
1. **密码找回**: 暂不支持邮件重置密码
2. **第三方登录**: 暂不支持 Google、GitHub 等登录
3. **新闻搜索**: 暂不支持关键词搜索
4. **国际化覆盖**: login.html 和 register.html 待完善

---

## 🔮 下一步计划

### 阶段四：后端 P1 优化
- [ ] 创建 BaseController 统一认证逻辑
- [ ] 统一异常处理和错误消息
- [ ] 清理 Service 层双版本方法
- [ ] 添加 API 文档（OpenAPI/Swagger）

### 阶段五：前端 P1 优化
- [ ] 创建 utils.js 工具模块
- [ ] 完善所有页面的国际化支持
- [ ] 消除代码重复（showMessage、formatTime 等）
- [ ] 优化移动端体验

### 阶段六：RSS 系统开发
- [ ] 替换 NewsAPI 为自主 RSS 抓取系统
- [ ] 支持 OPML 文件导入
- [ ] 实现文章持久化存储
- [ ] 添加收藏和分享功能
- [ ] 支持全文搜索

### 阶段七：测试提升
- [ ] 后端测试覆盖率提升到 60%+
- [ ] 前端测试覆盖率提升到 50%+
- [ ] 添加集成测试和 E2E 测试

---

## 📚 相关文档

- `CLAUDE.md` - 项目架构和开发指南
- `docs/STAGE2_GROUPA_COMPLETED.md` - Group A 功能完成总结
- `docs/STAGE3_GROUPB_COMPLETED.md` - Group B 功能完成总结
- `docs/P0_SECURITY_FIXES_SUMMARY.md` - P0 安全修复总结
- `docs/DOMAIN_SSL_SETUP_GUIDE.md` - SSL 配置指南
- `docs/DEPLOYMENT_QUICK_REFERENCE.md` - 部署快速参考

---

## 👥 贡献者

- **开发**: Claude Code
- **架构设计**: Claude Code + 用户需求
- **文档**: Claude Code

---

## 📄 许可证

本项目采用 MIT 许可证。

---

## 🎉 结语

v1.0 基线版本的发布标志着新闻订阅系统已经具备了完整的核心功能和生产就绪的稳定性。感谢所有参与开发的人员！

**下一个版本**: v1.1 (预计 2025 年 1 月下旬)
**主要目标**: RSS 系统集成、测试覆盖率提升、性能优化

---

**发布日期**: 2025年1月2日
**Git Tag**: v1.0
**Baseline Branch**: main

🤖 Generated with [Claude Code](https://claude.com/claude-code)
