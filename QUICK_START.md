# 🚀 新闻订阅应用 - 快速启动指南

恭喜！项目代码已经全部完成！

## 📋 项目完成情况

### ✅ 已完成
- [x] 开发环境配置
- [x] 项目结构创建
- [x] 前端项目初始化（HTML + CSS + JavaScript）
- [x] 后端 Spring Boot 项目
- [x] MySQL 数据库配置
- [x] 实体类（Entity）
- [x] Repository 接口
- [x] DTO 类
- [x] Service 业务逻辑
- [x] Controller API 端点
- [x] CORS 和 Security 配置
- [x] 前端页面（登录、注册、主页）
- [x] 前端样式和交互逻辑

### ⏳ 待完成
- [ ] 获取并配置 NewsAPI Key（必需！）

---

## 🔑 步骤 1：获取 NewsAPI Key

新闻功能需要 NewsAPI 的 API Key。

### 获取方法：

1. **访问注册页面**
   打开浏览器，访问：https://newsapi.org/register

2. **填写注册信息**
   - 用户名
   - 邮箱地址（使用真实邮箱）
   - 密码

3. **验证邮箱**
   - 检查您的邮箱
   - 点击验证链接

4. **获取 API Key**
   - 登录后，进入账户页面
   - 复制 API Key（格式类似：`abcd1234efgh5678`）

5. **配置到项目中**
   编辑文件：`backend/src/main/resources/application.properties`
   找到这一行：
   ```properties
   newsapi.api-key=your_newsapi_key_here
   ```
   替换为：
   ```properties
   newsapi.api-key=你的API_Key
   ```

---

## 🚀 步骤 2：启动应用

### 2.1 启动后端（Spring Boot）

```bash
# 进入后端目录
cd /Users/liangruchao/Documents/70-79\ Projects/72\ Code\ Learning\ Projects/72.01\ small\ demo/backend

# 启动 Spring Boot 应用
mvn spring-boot:run
```

**等待看到以下信息表示启动成功：**
```
Started NewsApplication in X.XXX seconds
News Subscription Application Started!
Access the application at: http://localhost:8081
```

**保持这个终端窗口打开！**

---

### 2.2 启动前端（新开一个终端窗口）

```bash
# 进入前端目录
cd /Users/liangruchao/Documents/70-79\ Projects/72\ Code\ Learning\ Projects/72.01\ small\ demo/frontend

# 启动前端服务器
npm run dev
```

**看到以下信息表示成功：**
```
Starting up http-server, serving ./
Available on:
  http://127.0.0.1:8080
  http://192.168.x.x:8080
```

---

## 🌐 步骤 3：访问应用

打开浏览器，访问：**http://localhost:8080**

---

## 🎯 测试流程

### 1. 注册新用户
- 点击"注册"
- 输入用户名、邮箱、密码
- 提交后会自动登录并跳转到主页

### 2. 订阅新闻类别
- 在主页选择新闻类别（如：科技、体育）
- 点击"订阅"按钮
- 订阅会显示在列表中

### 3. 查看新闻
- 点击"刷新新闻"按钮
- 系统会根据您的订阅获取新闻
- 点击新闻可以跳转到原文

### 4. 取消订阅
- 点击订阅卡片上的 × 按钮
- 确认后取消订阅

---

## 📁 项目结构

```
72.01 small demo/
├── frontend/              # 前端项目
│   ├── public/
│   │   ├── index.html    # 主页
│   │   ├── login.html    # 登录页
│   │   ├── register.html # 注册页
│   │   └── css/
│   │       └── style.css # 样式
│   ├── js/
│   │   ├── api.js        # API 封装
│   │   ├── auth.js       # 认证逻辑
│   │   └── news.js       # 新闻逻辑
│   └── package.json
│
├── backend/              # 后端项目
│   ├── src/main/java/com/newsapp/
│   │   ├── NewsApplication.java    # 启动类
│   │   ├── controller/             # 控制器
│   │   ├── service/                # 业务逻辑
│   │   ├── repository/             # 数据访问
│   │   ├── entity/                 # 实体类
│   │   ├── dto/                    # 数据传输对象
│   │   └── config/                 # 配置类
│   ├── src/main/resources/
│   │   └── application.properties  # 配置文件
│   └── pom.xml
│
└── database/
    └── init.sql          # 数据库脚本
```

---

## 🔧 常见问题

### Q1: 后端启动失败
**原因**：MySQL 未运行或密码错误
**解决**：
```bash
# 确保旧 MySQL 在运行
sudo /usr/local/mysql/support-files/mysql.server status

# 如果没运行，启动它
sudo /usr/local/mysql/support-files/mysql.server start
```

### Q2: 前端无法连接后端
**错误**：CORS 或 Network Error
**解决**：
- 确保后端已启动
- 检查后端端口是 8081
- 检查前端端口是 8080

### Q3: 新闻无法加载
**原因**：NewsAPI Key 未配置或无效
**解决**：
- 检查 application.properties 中的 API Key
- 确认已从 NewsAPI 获取有效 Key

### Q4: Session 丢失
**原因**：后端重启
**解决**：重新登录即可

---

## 📝 技术栈

### 前端
- HTML5 + CSS3
- 原生 JavaScript（ES6+）
- Fetch API
- http-server

### 后端
- Java 21
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security（简化版）
- MySQL Connector

### 数据库
- MySQL 9.x

### 外部服务
- NewsAPI（https://newsapi.org/）

---

## 🎨 功能特性

- ✅ 用户注册和登录
- ✅ Session 认证管理
- ✅ 新闻类别订阅（7个类别）
- ✅ 实时新闻获取
- ✅ 响应式设计
- ✅ 跨域支持
- ✅ 优雅的错误处理

---

## 🚀 下一步优化建议

1. **安全性**
   - 添加密码加密（BCrypt）
   - 添加输入验证
   - CSRF 保护

2. **功能增强**
   - 添加新闻搜索
   - 添加新闻收藏
   - 添加分页功能
   - 添加新闻缓存（Redis）

3. **用户体验**
   - 加载动画
   - 更好的错误提示
   - 移动端优化

4. **部署**
   - Docker 容器化
   - 云服务器部署

---

## 📞 需要帮助？

如果遇到问题：
1. 检查本文档的"常见问题"部分
2. 查看控制台错误信息
3. 确认所有服务都已启动

**祝您使用愉快！** 🎉
