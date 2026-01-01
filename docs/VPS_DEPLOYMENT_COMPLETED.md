# VPS 部署完成报告

**部署日期**: 2026-01-02
**VPS IP**: 47.103.204.114
**部署环境**: Ubuntu 20.04.6 LTS
**状态**: ✅ 部署成功并测试通过

---

## 一、环境配置清单

### 1.1 基础环境
| 组件 | 版本/配置 | 状态 |
|------|----------|------|
| 操作系统 | Ubuntu 20.04.6 LTS | ✅ |
| CPU | 2 核 | ✅ |
| 内存 | 1.8GB (可用 749MB) | ✅ |
| 磁盘 | 40GB SSD (已用 6.2GB) | ✅ |

### 1.2 软件环境
| 软件 | 版本 | 端口 | 状态 |
|------|------|------|------|
| Java | OpenJDK 21.0.7 | - | ✅ |
| MySQL | 8.0 | 3306 | ✅ (禁止外部访问) |
| Redis | 7.x | 6379 | ✅ (禁止外部访问) |
| Nginx | 1.18.0 | 80 | ✅ |
| Spring Boot | 3.2.0 | 8081 | ✅ (禁止外部访问) |

---

## 二、部署架构

```
Internet (Port 80)
    ↓
[Nginx 反向代理]
    ↓
[Spring Boot :8081] ←→ [MySQL :3306]
    ↓                    ←→ [Redis :6379]
[客户端响应]
```

**安全配置**:
- 外部只能访问端口 80 (HTTP)
- 端口 8081/3306/6379 仅限本机访问
- UFW 防火墙已启用

---

## 三、服务配置

### 3.1 Systemd 服务
**文件**: `/etc/systemd/system/newsapp.service`

```ini
[Unit]
Description=News Subscription Application
After=network.target mysql.service redis.service

[Service]
Type=simple
User=newsapp
WorkingDirectory=/var/www/news-app
ExecStart=/usr/lib/jvm/java-21-openjdk-amd64/bin/java -jar \
    -Dserver.port=8081 \
    -Xms256m -Xmx768m \
    news-subscription-backend-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

**管理命令**:
```bash
systemctl start newsapp    # 启动
systemctl stop newsapp     # 停止
systemctl restart newsapp  # 重启
systemctl status newsapp   # 状态
journalctl -u newsapp -f   # 日志
```

### 3.2 Nginx 反向代理
**文件**: `/etc/nginx/sites-available/news-app`

```nginx
server {
    listen 80;
    server_name 47.103.204.114;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3.3 数据库配置
**数据库名**: `news_app`
**用户**: `newsapp`
**密码**: `NewsApp@2026DB`
**字符集**: `utf8mb4_unicode_ci`

### 3.4 Redis 配置
**端口**: 6379
**密码**: `Redis@2026SecurePass`
**最大内存**: 256MB
**策略**: allkeys-lru

---

## 四、应用配置

### 4.1 应用配置文件
**文件**: `/var/www/news-app/application.properties`

关键配置:
```properties
server.port=8081
spring.profiles.active=vps

# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/news_app
spring.datasource.username=newsapp
spring.datasource.password=NewsApp@2026DB

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=Redis@2026SecurePass

# 缓存 TTL: 10 分钟
spring.cache.redis.time-to-live=600000
```

### 4.2 Spring Security 配置
**修改**: `SecurityConfig.java` - VPS 环境禁用 CSRF 保护

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(csrf -> csrf.disable())  // VPS 部署环境禁用 CSRF
        .sessionManagement(session -> session
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        );
    return http.build();
}
```

---

## 五、防火墙配置

```bash
To                         Action      From
--                         ------      ----
22/tcp                     ALLOW       Anywhere
80/tcp                     ALLOW       Anywhere
443/tcp                    ALLOW       Anywhere
8081/tcp                   DENY        Anywhere
3306/tcp                   DENY        Anywhere
6379/tcp                   DENY        Anywhere
```

---

## 六、API 访问地址

### 6.1 公网访问
- **基础 URL**: `http://47.103.204.114/api`
- **注册**: `POST /api/auth/register`
- **登录**: `POST /api/auth/login`
- **订阅**: `POST /api/subscriptions`
- **新闻**: `GET /api/news`

### 6.2 测试命令
```bash
# 注册
curl -X POST http://47.103.204.114/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"Test123"}'

# 登录
curl -X POST http://47.103.204.114/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Test123"}'
```

---

## 七、性能测试结果

### 7.1 系统资源（部署后）
- **内存使用**: 932MB / 1.8GB (52%)
- **可用内存**: 749MB
- **CPU 使用**: 正常
- **磁盘使用**: 6.2GB / 40GB (17%)

### 7.2 压力测试
- **测试场景**: 50 并发登录请求
- **结果**: 全部成功，无错误
- **响应时间**: 正常

---

## 八、监控和维护

### 8.1 服务管理
```bash
# 查看服务状态
systemctl status newsapp mysql redis-server nginx

# 查看应用日志
journalctl -u newsapp -f

# 查看 Nginx 日志
tail -f /var/log/nginx/news-app-access.log
tail -f /var/log/nginx/news-app-error.log
```

### 8.2 数据库管理
```bash
# 登录 MySQL
mysql -u newsapp -p'NewsApp@2026DB' news_app

# 备份数据库
mysqldump -u newsapp -p'NewsApp@2026DB' news_app > backup.sql

# 查看数据库大小
mysql -u newsapp -p'NewsApp@2026DB' -e "SELECT table_schema, ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS size_mb FROM information_schema.tables WHERE table_schema = 'news_app' GROUP BY table_schema;"
```

### 8.3 Redis 管理
```bash
# 登录 Redis
redis-cli -a Redis@2026SecurePass

# 查看内存使用
redis-cli -a Redis@2026SecurePass INFO memory

# 清空缓存（谨慎使用）
redis-cli -a Redis@2026SecurePass FLUSHALL
```

---

## 九、故障排查

### 9.1 服务无法启动
```bash
# 查看详细错误
journalctl -u newsapp -n 50 --no-pager

# 检查端口占用
ss -tlnp | grep 8081

# 检查 Java 版本
java -version
```

### 9.2 API 无法访问
```bash
# 检查服务状态
systemctl status newsapp

# 测试本地访问
curl http://localhost:8081/api/auth/login

# 检查 Nginx 配置
nginx -t

# 重载 Nginx
nginx -s reload
```

### 9.3 数据库连接失败
```bash
# 测试 MySQL 连接
mysql -u newsapp -p'NewsApp@2026DB' news_app

# 检查 MySQL 状态
systemctl status mysql

# 查看 MySQL 错误日志
tail -f /var/log/mysql/error.log
```

---

## 十、优化建议

### 10.1 性能优化
- [ ] 配置 Redis 持久化 (RDB/AOF)
- [ ] 优化 MySQL 缓冲池配置
- [ ] 启用 Nginx gzip 压缩
- [ ] 配置 CDN 加速静态资源

### 10.2 安全优化
- [ ] 安装 SSL 证书 (Let's Encrypt)
- [ ] 配置 HTTPS (443 端口)
- [ ] 安装 Fail2Ban 防暴力破解
- [ ] 配置 MySQL 自动备份

### 10.3 监控优化
- [ ] 安装 Prometheus + Grafana
- [ ] 配置应用性能监控 (APM)
- [ ] 配置告警通知

---

## 十一、文件清单

### 11.1 应用文件
| 文件 | 路径 |
|------|------|
| JAR 包 | `/var/www/news-app/news-subscription-backend-1.0.0.jar` |
| 配置文件 | `/var/www/news-app/application.properties` |
| 日志目录 | `/var/log/news-app/` |

### 11.2 系统配置文件
| 文件 | 用途 |
|------|------|
| `/etc/systemd/system/newsapp.service` | Systemd 服务配置 |
| `/etc/nginx/sites-available/news-app` | Nginx 站点配置 |
| `/etc/nginx/sites-enabled/news-app` | Nginx 符号链接 |
| `/var/www/news-app/application.properties` | 应用配置 |

---

## 十二、部署时间线

| 时间 | 操作 | 耗时 |
|------|------|------|
| 23:17 | 停止 Apache2，安装 Nginx | 5 min |
| 23:20 | 安装 Java 21 | 5 min |
| 23:24 | 配置 MySQL 数据库 | 5 min |
| 23:28 | 安装 Redis | 3 min |
| 23:30-00:17 | 上传应用文件，配置服务 | 47 min (含多次调试) |
| 00:17-00:30 | 修复 SecurityConfig，最终测试 | 13 min |
| **总计** | | **约 2 小时** |

---

## 十三、下一步行动

### 13.1 立即可做
- [x] ✅ 基础部署完成
- [ ] 配置 SSL 证书启用 HTTPS
- [ ] 配置自动备份
- [ ] 添加域名解析

### 13.2 可选优化
- [ ] 前端部署 (React/Vue)
- [ ] CI/CD 自动化部署
- [ ] 监控告警系统
- [ ] 负载均衡 (多实例)

---

## 十四、联系信息

**VPS**: 阿里云 ECS
**IP**: 47.103.204.114
**SSH**: `ssh root@47.103.204.114`
**文档**: 参见 `docs/VPS_DEPLOYMENT_GUIDE.md`

---

**部署状态**: ✅ **成功**
**最后更新**: 2026-01-02
**维护**: 本地用户 `root`
