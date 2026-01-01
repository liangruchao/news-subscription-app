# 第三阶段功能优化 - 开发计划

**开始日期**: 2026-01-02
**开发模式**: 并行开发
**分支策略**: Feature Branch Workflow

---

## 分支结构

```
main (生产环境)
├── develop (开发环境)
│   ├── feature/auth-subscription (A组)
│   └── feature/profile-notification (B组)
```

---

## 并行开发规划

### A 组：核心功能 (feature/auth-subscription)

**目标**: 完善用户认证与新闻订阅核心体验

**模块 1: 用户认证与授权**

#### 已有功能
- ✅ 用户注册 (DTO + Service + Controller)
- ✅ 用户登录
- ✅ Session 认证
- ✅ 基础权限配置

#### 待开发功能
- [ ] JWT 令牌认证
  - [ ] JWT 工具类
  - [ ] 登录返回 JWT Token
  - [ ] JWT 过滤器
  - [ ] Token 刷新机制
  - [ ] 登出失效 Token

- [ ] 密码安全
  - [ ] BCrypt 密码加密
  - [ ] 密码强度验证
  - [ ] 密码重置（邮件）
  - [ ] 修改密码

- [ ] 邮箱验证
  - [ ] 注册邮箱验证
  - [ ] 验证码生成
  - [ ] 邮件服务集成
  - [ ] 验证链接发送

- [ ] 记住登录
  - [ ] Refresh Token
  - [ ] 长期 Token

- [ ] 第三方登录（可选）
  - [ ] GitHub OAuth
  - [ ] Google OAuth

- [ ] 权限管理
  - [ ] 角色枚举（USER, ADMIN）
  - [ ] 权限注解
  - [ ] 管理员接口

**模块 2: 新闻订阅功能**

#### 已有功能
- ✅ 基础订阅功能
- ✅ NewsAPI 集成

#### 待开发功能
- [ ] 订阅分类优化
  - [ ] 分类枚举扩展
  - [ ] 分类管理接口
  - [ ] 多选订阅

- [ ] 个性化推荐
  - [ ] 阅读历史记录
  - [ ] 推荐算法
  - [ ] 热门新闻

- [ ] 文章收藏
  - [ ] 收藏功能
  - [ ] 收藏列表
  - [ ] 取消收藏

- [ ] 新闻搜索
  - [ ] 关键词搜索
  - [ ] 高级筛选
  - [ ] 搜索历史

- [ ] 阅读历史
  - [ ] 阅读记录
  - [ ] 阅读进度
  - [ ] 历史清理

---

### B 组：增强功能 (feature/profile-notification)

**目标**: 提升用户体验和系统互动

**模块 3: 用户个人中心**

#### 待开发功能
- [ ] 用户资料
  - [ ] 资料展示接口
  - [ ] 资料修改
  - [ ] 头像上传
  - [ ] 个人简介

- [ ] 账户安全
  - [ ] 密码修改
  - [ ] 邮箱绑定
  - [ ] 登录设备管理
  - [ ] 账户注销

- [ ] 偏好设置
  - [ ] 订阅偏好
  - [ ] 通知设置
  - [ ] 显示设置

- [ ] 数据统计
  - [ ] 阅读统计
  - [ ] 订阅统计
  - [ ] 活跃度统计

- [ ] 订阅管理
  - [ ] 我的订阅列表
  - [ ] 批量管理
  - [ ] 推荐订阅

**模块 4: 通知与消息系统**

#### 待开发功能
- [ ] 邮件通知
  - [ ] 邮件服务集成
  - [ ] 每日新闻摘要
  - [ ] 订阅更新通知
  - [ ] 系统通知

- [ ] 系统公告
  - [ ] 公告管理
  - [ ] 公告展示
  - [ ] 已读标记

- [ ] 站内消息
  - [ ] 消息模型
  - [ ] 消息列表
  - [ ] 未读标记
  - [ ] 消息详情

- [ ] 实时推送
  - [ ] WebSocket 支持
  - [ ] 推送服务
  - [ ] 在线状态

- [ ] 通知偏好
  - [ ] 通知类型管理
  - [ ] 频率设置
  - [ ] 免打扰模式

---

## 技术实现

### JWT 认证
```java
// 依赖
implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
```

### 邮件服务
```java
// Spring Mail
implementation 'org.springframework.boot:spring-boot-starter-mail'

// 异步任务
@EnableAsync
```

### WebSocket
```java
// Spring WebSocket
implementation 'org.springframework.boot:spring-boot-starter-websocket'
```

### 定时任务
```java
// Spring Scheduling
@EnableScheduling
@Scheduled(cron = "0 0 8 * * ?")  // 每日8点
```

---

## 开发流程

### 1. 功能开发
- 在对应的 feature 分支开发
- 遵循代码规范
- 编写单元测试

### 2. 代码审查
- 提交 Pull Request
- Code Review
- 修改建议

### 3. 测试验证
- 单元测试通过
- 集成测试通过
- 手动功能测试

### 4. 合并发布
- 合并到 develop 分支
- Staging 环境测试
- 通过后合并到 main
- Production 发布

---

## 文件结构

```
backend/src/main/java/com/newsapp/
├── auth/               # 认证模块 (A组)
│   ├── jwt/
│   ├── oauth/
│   └── security/
├── subscription/       # 订阅模块 (A组)
│   ├── recommendation/
│   ├── favorite/
│   └── search/
├── profile/            # 个人中心 (B组)
│   ├── avatar/
│   ├── settings/
│   └── statistics/
├── notification/       # 通知系统 (B组)
│   ├── email/
│   ├── message/
│   ├── announcement/
│   └── websocket/
└── common/             # 公共模块
    ├── email/
    ├── utils/
    └── exceptions/
```

---

## 里程碑

| 阶段 | 功能 | 目标日期 |
|------|------|----------|
| M1 | JWT 认证 + 邮箱验证 | Day 3 |
| M2 | 订阅优化 + 收藏功能 | Day 5 |
| M3 | 个人中心基础功能 | Day 7 |
| M4 | 邮件通知系统 | Day 10 |
| M5 | 消息与推送系统 | Day 12 |
| M6 | 测试与优化 | Day 15 |

---

## 依赖服务

### 新增依赖
- **邮件服务**: SMTP 阿里云/腾讯云
- **存储服务**: OSS 阿里云/腾讯云 (头像)
- **推送服务**: WebSocket / 第三方推送

### 外部 API
- **NewsAPI.org**: 已有
- **邮件 API**: SMTP
- **OAuth**: GitHub/Google

---

## 注意事项

1. **代码规范**: 遵循现有代码风格
2. **测试先行**: 核心功能必须有单元测试
3. **安全第一**: 用户数据加密存储
4. **性能考虑**: 缓存优化、异步处理
5. **兼容性**: 不破坏现有功能

---

**开始开发**: 2026-01-02
**预计完成**: 2026-01-17
