# Stage 2: Group A 基础功能开发完成总结

## 概述

**阶段目标**: 实现 A 组（基础功能）的前后端功能

**开发时间**: 2024年12月 - 2025年1月

**技术栈**:
- 后端: Spring Boot 3.2.0 + Java 21 + MySQL + NewsAPI.org
- 前端: 原生 JavaScript (ES6+) + HTML5 + CSS3
- 认证: Session-based Authentication
- 密码加密: BCrypt

---

## 功能清单

### 1. 用户认证 (Authentication)

#### 前端页面
- ✅ 用户注册 (register.html)
- ✅ 用户登录 (login.html)
- ✅ 登录状态检查
- ✅ 自动登录跳转

#### 后端 API
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/current` | GET | 获取当前登录用户 |

#### 功能特性
- ✅ 用户名唯一性验证
- ✅ 邮箱唯一性验证
- ✅ BCrypt 密码加密
- ✅ Session 会话管理
- ✅ 自动登录（注册后自动创建 Session）
- ✅ 登录状态持久化（30分钟 Session 超时）

#### 实现文件
- `frontend/public/login.html` - 登录页面
- `frontend/public/register.html` - 注册页面
- `frontend/public/js/auth.js` - 认证模块
- `frontend/public/js/api.js` - API 封装
- `backend/.../AuthController.java` - 认证控制器
- `backend/.../UserService.java` - 用户服务
- `backend/.../User.java` - 用户实体
- `backend/.../UserRepository.java` - 用户数据访问层

---

### 2. 新闻订阅管理 (Subscription)

#### 前端页面
- ✅ 订阅列表展示
- ✅ 添加订阅（下拉选择）
- ✅ 取消订阅（二次确认）
- ✅ 订阅类别中文名映射

#### 后端 API
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/subscriptions` | GET | 获取订阅列表 |
| `/api/subscriptions` | POST | 添加订阅 |
| `/api/subscriptions/{category}` | DELETE | 取消订阅 |

#### 支持的新闻类别
| 类别代码 | 中文名称 |
|----------|----------|
| `business` | 商业 |
| `entertainment` | 娱乐 |
| `general` | 综合 |
| `health` | 健康 |
| `science` | 科学 |
| `sports` | 体育 |
| `technology` | 科技 |

#### 功能特性
- ✅ 用户与类别多对多关系
- ✅ 防止重复订阅（数据库唯一约束）
- ✅ 用户级联删除（删除用户时自动删除订阅）
- ✅ 实时订阅列表更新

#### 实现文件
- `frontend/public/index.html` - 订阅管理界面
- `frontend/public/js/news.js` - 订阅业务逻辑
- `backend/.../SubscriptionController.java` - 订阅控制器
- `backend/.../SubscriptionService.java` - 订阅服务
- `backend/.../Subscription.java` - 订阅实体
- `backend/.../SubscriptionRepository.java` - 订阅数据访问层

---

### 3. 新闻内容展示 (News Feed)

#### 前端页面
- ✅ 新闻列表展示
- ✅ 新闻卡片布局（标题、描述、图片、来源）
- ✅ 新闻链接跳转
- ✅ 刷新新闻按钮
- ✅ 加载状态提示

#### 后端 API
| 端点 | 方法 | 功能 |
|------|------|------|
| `/api/news` | GET | 获取用户订阅的新闻 |
| `/api/news/category/{category}` | GET | 获取指定类别新闻 |

#### 功能特性
- ✅ 基于 NewsAPI.org 获取实时新闻
- ✅ 根据用户订阅动态聚合新闻
- ✅ 新闻图片懒加载（失败隐藏）
- ✅ 新闻来源显示
- ✅ 外部链接打开

#### 实现文件
- `frontend/public/index.html` - 新闻展示界面
- `frontend/public/js/news.js` - 新闻业务逻辑
- `backend/.../NewsController.java` - 新闻控制器
- `backend/.../NewsService.java` - 新闻服务（NewsAPI 集成）
- `backend/.../NewsDto.java` - 新闻数据传输对象

---

## 数据库表结构

### users (用户表)
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### subscriptions (订阅表)
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

---

## 技术架构

### 后端分层架构
```
┌─────────────────────────────────────┐
│     Controller Layer (REST API)     │
│  AuthController                     │
│  SubscriptionController             │
│  NewsController                     │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│       Service Layer (Business)      │
│  UserService                        │
│  SubscriptionService                │
│  NewsService                        │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│    Repository Layer (Data Access)   │
│  UserRepository                     │
│  SubscriptionRepository             │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│         Database (MySQL)            │
│  users                              │
│  subscriptions                      │
└─────────────────────────────────────┘
```

### 前端模块设计
```
┌─────────────────────────────────────┐
│         HTML Pages                  │
│  index.html (首页)                  │
│  login.html (登录)                  │
│  register.html (注册)               │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│     JavaScript Modules              │
│  auth.js (认证模块)                 │
│  api.js (HTTP 客户端)               │
│  news.js (订阅和新闻)               │
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│       REST API Endpoints            │
│  /api/auth/*                        │
│  /api/subscriptions/*               │
│  /api/news/*                        │
└─────────────────────────────────────┘
```

---

## 技术亮点

### 1. Session 认证机制
```java
// 登录时创建 Session
@PostMapping("/login")
public ApiResponse<User> login(@RequestBody LoginRequest request, HttpSession session) {
    User user = userService.login(request);
    session.setAttribute("user", user);
    return ApiResponse.success("登录成功", user);
}

// 每个请求通过 Session 获取用户
private User getCurrentUser(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        throw new RuntimeException("请先登录");
    }
    return user;
}
```

### 2. BCrypt 密码加密
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

// 注册时加密
String encodedPassword = passwordEncoder.encode(rawPassword);
user.setPassword(encodedPassword);

// 登录时验证
if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
    throw new RuntimeException("用户名或密码错误");
}
```

### 3. 数据库唯一约束
```sql
-- 防止用户重复订阅同一类别
UNIQUE KEY unique_subscription (user_id, category)
```

### 4. 级联删除
```sql
-- 删除用户时自动删除其订阅记录
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
```

### 5. NewsAPI 集成
```java
// 根据用户订阅的类别获取新闻
public List<NewsDto> getUserNews(List<String> categories) {
    // 调用 NewsAPI.org 获取实时新闻
    // 聚合多个类别的新闻结果
    // 返回统一的 NewsDto 格式
}
```

### 6. 构造器注入
```java
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
}
```

### 7. 统一 API 响应格式
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

---

## 配置说明

### 后端配置 (application.properties)
```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/news_app
spring.datasource.username=root
spring.datasource.password=root123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Session 配置
server.servlet.session.timeout=30m

# NewsAPI 配置
newsapi.key=your_newsapi_key_here
newsapi.base-url=https://newsapi.org/v2
```

### 前端配置
```javascript
// API 基础路径
const API_BASE_URL = 'http://localhost:8081';

// 请求拦截器（自动添加 Session）
axios.defaults.withCredentials = true;

// 响应拦截器（统一处理错误）
axios.interceptors.response.use(
    response => response.data,
    error => {
        console.error('API Error:', error);
        return Promise.reject(error);
    }
);
```

---

## 外部依赖

### NewsAPI.org
- **用途**: 获取实时新闻数据
- **注册地址**: https://newsapi.org/register
- **免费额度**: 每天 100 次请求
- **支持类别**: business, entertainment, general, health, science, sports, technology

### MySQL 9.5.0
- **端口**: 3306
- **数据库**: news_app
- **字符集**: utf8mb4

---

## 测试验证

### 功能测试
- ✅ 用户注册（用户名/邮箱唯一性验证）
- ✅ 用户登录（密码验证）
- ✅ 登录状态检查
- ✅ 添加订阅（防止重复）
- ✅ 取消订阅
- ✅ 获取订阅列表
- ✅ 获取用户新闻
- ✅ 新闻图片显示
- ✅ 新闻链接跳转

### 兼容性测试
- ✅ Chrome 浏览器
- ✅ Firefox 浏览器
- ✅ Safari 浏览器
- ✅ 移动端响应式布局

### 安全性测试
- ✅ 密码 BCrypt 加密
- ✅ Session 会话管理
- ✅ SQL 注入防护（JPA）
- ✅ XSS 防护（前端转义）

---

## 文件清单

### 后端文件
#### Controller 层
- ✅ `AuthController.java` - 认证控制器
- ✅ `SubscriptionController.java` - 订阅控制器
- ✅ `NewsController.java` - 新闻控制器

#### Service 层
- ✅ `UserService.java` - 用户服务
- ✅ `SubscriptionService.java` - 订阅服务
- ✅ `NewsService.java` - 新闻服务

#### Entity 层
- ✅ `User.java` - 用户实体
- ✅ `Subscription.java` - 订阅实体

#### Repository 层
- ✅ `UserRepository.java` - 用户数据访问
- ✅ `SubscriptionRepository.java` - 订阅数据访问

#### DTO 层
- ✅ `ApiResponse.java` - 统一响应格式
- ✅ `LoginRequest.java` - 登录请求
- ✅ `RegisterRequest.java` - 注册请求
- ✅ `SubscriptionRequest.java` - 订阅请求
- ✅ `NewsDto.java` - 新闻数据传输对象

#### Config 层
- ✅ `SecurityConfig.java` - Spring Security 配置

### 前端文件
#### HTML 页面
- ✅ `index.html` - 首页（订阅+新闻）
- ✅ `login.html` - 登录页面
- ✅ `register.html` - 注册页面

#### CSS 样式
- ✅ `style.css` - 全局样式

#### JavaScript 模块
- ✅ `api.js` - API 封装
- ✅ `auth.js` - 认证模块
- ✅ `news.js` - 订阅和新闻模块

---

## 已知限制

### NewsAPI 限制
- 免费版每天只支持 100 次请求
- 需要申请 API Key
- 某些类别新闻可能较少

### 认证限制
- Session 认证不支持分布式部署
- 需要配置 Session 共享才能支持多实例

### 功能限制
- 暂不支持第三方登录（Google, GitHub 等）
- 暂不支持密码找回功能
- 暂不支持新闻搜索功能

---

## 后续优化建议

### 短期优化
1. 添加邮件验证功能
2. 实现密码找回功能
3. 添加新闻搜索功能
4. 优化新闻加载性能（缓存、分页）

### 长期规划
1. 实现 JWT 认证（支持分布式）
2. 添加第三方登录支持
3. 实现新闻推荐算法
4. 添加用户行为分析

---

## 与 Stage 3 (Group B) 的关系

### Stage 2 (Group A) 完成的功能
- ✅ 用户认证
- ✅ 新闻订阅管理
- ✅ 新闻内容展示

### Stage 3 (Group B) 扩展的功能
- ✅ 个人中心（Profile）
- ✅ 消息中心（Messages）
- ✅ 系统公告（Announcements）
- ✅ 用户偏好设置（Preferences）
- ✅ 国际化支持（i18n）

### 数据关联
- Group A 的 `users` 表被 Group B 的所有功能引用
- Group A 的订阅数据在 Group B 的个人中心显示统计信息
- Group A 的新闻数据在 Group B 的偏好设置中配置显示数量

---

## 提交信息

**分支**: `main` (已合并到 `feature/auth-subscription`)

**提交内容**:
- 用户认证完整实现
- 新闻订阅管理完整实现
- 新闻内容展示完整实现
- Session 认证机制
- BCrypt 密码加密
- NewsAPI.org 集成

**测试状态**: ✅ 全部通过

---

## 总结

Stage 2 成功完成了 A 组（基础功能）的所有开发，为用户提供了完整的新闻订阅系统核心功能。该阶段建立了稳固的技术基础，包括 Session 认证、数据库设计、REST API 架构以及前端模块化开发模式。

**关键成果**:
- 🔐 完整的用户认证系统
- 📰 新闻订阅管理功能
- 🌐 NewsAPI.org 集成
- 🛡️ BCrypt 密码加密
- 🏗️ 清晰的分层架构

**下一阶段**: Stage 3 Group B 个人中心功能（已完成）
