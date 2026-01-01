# 📰 新闻订阅系统 (News Subscription System)

一个简单的新闻订阅应用，支持用户注册、订阅新闻类别、查看个性化新闻。

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![MySQL](https://img.shields.io/badge/MySQL-9.0-blue)
![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-yellow)

## ✨ 功能特性

- 👤 用户注册和登录（Session 认证）
- 📂 订阅 7 种新闻类别（商业、娱乐、综合、健康、科学、体育、科技）
- 📰 实时获取英文新闻（通过 [NewsAPI](https://newsapi.org/)）
- 🎨 响应式前端界面
- 🔒 RESTful API 设计
- 💾 MySQL 数据持久化

## 🛠️ 技术栈

### 前端
- **HTML5 + CSS3** - 页面结构和样式
- **JavaScript ES6+** - 交互逻辑
- **Axios** - HTTP 客户端
- **http-server** - 开发服务器

### 后端
- **Java 21** - 编程语言
- **Spring Boot 3.2** - 应用框架
- **Spring Data JPA** - ORM 框架
- **Spring Security** - 安全认证
- **MySQL Connector** - 数据库驱动

### 数据库
- **MySQL 9.x** - 关系型数据库

### 外部服务
- **[NewsAPI](https://newsapi.org/)** - 新闻数据源

## 📋 项目结构

```
.
├── frontend/              # 前端项目
│   ├── public/          # 静态资源
│   │   ├── css/         # 样式文件
│   │   ├── js/          # JavaScript 文件
│   │   ├── index.html   # 主页
│   │   ├── login.html   # 登录页
│   │   └── register.html# 注册页
│   └── package.json     # 前端依赖
│
├── backend/             # 后端项目
│   ├── src/main/java/com/newsapp/
│   │   ├── controller/  # REST API 控制器
│   │   ├── service/     # 业务逻辑层
│   │   ├── repository/  # 数据访问层
│   │   ├── entity/      # 实体类
│   │   ├── dto/         # 数据传输对象
│   │   └── config/      # 配置类
│   ├── src/main/resources/
│   │   └── application.properties # 应用配置
│   └── pom.xml          # Maven 配置
│
└── database/            # 数据库脚本
    └── init.sql        # 数据库初始化
```

## 🚀 快速开始

### 环境要求

- Node.js 18+ 或 20+
- Java 21
- Maven 3.9+
- MySQL 8.x 或 9.x

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/liangruchao/news-subscription-app.git
cd news-subscription-app
```

#### 2. 配置数据库

创建数据库并初始化表：
```bash
mysql -u root -p
CREATE DATABASE news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;
```

#### 3. 配置后端

编辑 `backend/src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.username=root
spring.datasource.password=your_password

# NewsAPI 配置
newsapi.api-key=your_newsapi_key_here
```

**获取 NewsAPI Key：**
1. 访问 https://newsapi.org/register
2. 注册免费账号
3. 获取 API Key

#### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8081 启动

#### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:8080 启动

#### 6. 访问应用

打开浏览器访问：http://localhost:8080

## 📱 使用说明

### 注册和登录

1. 访问注册页面
2. 填写用户名、邮箱和密码
3. 注册成功后自动登录

### 订阅新闻

1. 从下拉菜单选择新闻类别
2. 点击"订阅"按钮
3. 可订阅多个类别

### 查看新闻

1. 点击"刷新新闻"按钮
2. 系统根据您的订阅获取新闻
3. 点击新闻标题可跳转到原文

## 🔧 API 端点

### 认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/current` - 获取当前用户

### 新闻
- `GET /api/news` - 获取用户订阅的新闻
- `GET /api/news/category/{category}` - 获取指定类别的新闻

### 订阅
- `GET /api/subscriptions` - 获取用户订阅列表
- `POST /api/subscriptions` - 订阅类别
- `DELETE /api/subscriptions/{category}` - 取消订阅

## 🎯 支持的新闻类别

- 📊 Business（商业）
- 🎬 Entertainment（娱乐）
- 📰 General（综合）
- 🏥 Health（健康）
- 🔬 Science（科学）
- ⚽ Sports（体育）
- 💻 Technology（科技）

## 📸 截图

待添加...

## 🔐 安全说明

**注意**：此项目为学习项目，安全性较为基础：
- 密码未加密（建议使用 BCrypt）
- 无 CSRF 保护
- Session 超时时间 30 分钟

生产环境使用前请加强安全措施。

## 🛣️ 未来优化

- [ ] 添加密码加密（BCrypt）
- [ ] 添加新闻搜索功能
- [ ] 添加新闻收藏功能
- [ ] 实现分页显示
- [ ] 添加 Redis 缓存
- [ ] 添加单元测试和集成测试
- [ ] Docker 容器化部署
- [ ] CI/CD 自动化部署

## 📄 许可证

MIT License

## 👨‍💻 作者

Created with ❤️ using Spring Boot and vanilla JavaScript

---

**注意**：NewsAPI 免费版每天限制 100 次请求。

🤖 Generated with [Claude Code](https://claude.com/claude-code)
