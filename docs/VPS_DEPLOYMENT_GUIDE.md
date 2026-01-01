# VPS 环境配置指南 - 方案B标准配置

**目标系统**: Ubuntu 20.04/22.04 LTS
**配置日期**: 2026-01-01

---

## 一、方案B标准配置规格

| 组件 | 版本/规格 | 说明 |
|------|----------|------|
| 操作系统 | Ubuntu 20.04/22.04 LTS | 稳定版本 |
| Java | OpenJDK 21 | 后端运行环境 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.x | 缓存服务 |
| Nginx | 1.18+ | 反向代理 |
| 内存 | ≥ 2GB | 保证服务稳定运行 |
| 存储 | ≥ 40GB SSD | 系统和数据存储 |

---

## 二、环境检查和准备

### 2.1 连接到 VPS
```bash
# SSH 连接（替换为你的实际信息）
ssh root@your_vps_ip

# 或使用密钥
ssh -i /path/to/key.pem root@your_vps_ip
```

### 2.2 系统信息检查
```bash
# 查看系统版本
cat /etc/os-release

# 查看内存和CPU
free -h
lscpu | grep "^CPU(s):"

# 查看磁盘空间
df -h
```

### 2.3 更新系统
```bash
# 更新软件包列表
apt update

# 升级已安装的软件包
apt upgrade -y

# 安装基础工具
apt install -y curl wget vim git ufw
```

---

## 三、Java 21 安装配置

### 3.1 检查现有 Java 版本
```bash
java -version
```

### 3.2 安装 OpenJDK 21

#### 方法一：使用 SDKMAN（推荐）
```bash
# 安装 SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init"

# 安装 Java 21
sdk install java 21.0.1-tem

# 设置为默认版本
sdk default java 21.0.1-tem

# 验证安装
java -version
```

#### 方法二：使用 APT（Ubuntu 22.04+）
```bash
# 添加 OpenJDK PPA
add-apt-repository ppa:openjdk-r/ppa -y
apt update

# 安装 OpenJDK 21
apt install -y openjdk-21-jdk

# 验证安装
java -version
```

#### 方法三：手动安装（通用方法）
```bash
# 下载 OpenJDK 21
cd /opt
wget https://download.java.net/java/GA/jdk21.0.1/f228c843f9c94acad570fba21004558/12/GPL/openjdk-21.0.1_linux-x64_bin.tar.gz

# 解压
tar -xzf openjdk-21.0.1_linux-x64_bin.tar.gz
mv jdk-21.0.1 java21

# 设置环境变量
cat >> /etc/profile.d/java21.sh << 'EOF'
export JAVA_HOME=/opt/java21
export PATH=$JAVA_HOME/bin:$PATH
EOF

# 加载环境变量
source /etc/profile.d/java21.sh

# 验证安装
java -version
```

### 3.3 配置 JAVA_HOME
```bash
# 查找 Java 安装路径
readlink -f $(which java)

# 设置全局环境变量（根据实际路径修改）
cat >> /etc/environment << 'EOF'
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
EOF

# 重新加载
source /etc/environment

# 验证
echo $JAVA_HOME
```

---

## 四、MySQL 8.0 安装配置

### 4.1 安装 MySQL
```bash
# 安装 MySQL Server
apt install -y mysql-server

# 启动 MySQL
systemctl start mysql
systemctl enable mysql

# 检查状态
systemctl status mysql
```

### 4.2 安全配置
```bash
# 运行安全配置脚本
mysql_secure_installation

# 配置选项建议：
# - 设置 root 密码（复杂密码）
# - 删除匿名用户：Y
# - 禁止 root 远程登录：Y
# - 删除 test 数据库：Y
# - 重新加载权限表：Y
```

### 4.3 创建应用数据库和用户
```bash
# 登录 MySQL
mysql -u root -p

# 执行以下 SQL（在 MySQL 命令行中）
```

```sql
-- 创建数据库
CREATE DATABASE news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建应用用户（替换密码）
CREATE USER 'newsapp'@'localhost' IDENTIFIED BY 'your_strong_password_here';

-- 授予权限
GRANT ALL PRIVILEGES ON news_app.* TO 'newsapp'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 4.4 配置远程访问（可选）
```bash
# 编辑 MySQL 配置
vim /etc/mysql/mysql.conf.d/mysqld.cnf

# 修改 bind-address
bind-address = 0.0.0.0

# 重启 MySQL
systemctl restart mysql
```

### 4.5 防火墙配置
```bash
# 允许 MySQL 端口（如果需要远程访问）
ufw allow 3306/tcp

# 或仅允许本地访问
ufw deny 3306/tcp
```

---

## 五、Redis 安装配置

### 5.1 安装 Redis
```bash
# 安装 Redis
apt install -y redis-server

# 启动 Redis
systemctl start redis-server
systemctl enable redis-server

# 检查状态
systemctl status redis-server
```

### 5.2 配置 Redis
```bash
# 编辑配置文件
vim /etc/redis/redis.conf

# 关键配置项：
```

```conf
# 绑定地址（允许本地访问）
bind 127.0.0.1

# 端口
port 6379

# 设置密码（推荐）
requirepass your_redis_password_here

# 持久化配置
save 900 1
save 300 10
save 60 10000

# 最大内存限制
maxmemory 256mb
maxmemory-policy allkeys-lru
```

```bash
# 重启 Redis
systemctl restart redis-server

# 测试连接
redis-cli
AUTH your_redis_password_here
PING  # 应返回 PONG
EXIT
```

### 5.3 防火墙配置
```bash
# Redis 通常不需要远程访问
ufw deny 6379/tcp
```

---

## 六、应用部署配置

### 6.1 创建应用目录
```bash
# 创建应用目录
mkdir -p /var/www/news-app
cd /var/www/news-app

# 创建应用用户（安全考虑）
useradd -r -s /bin/false newsapp
chown -R newsapp:newsapp /var/www/news-app
```

### 6.2 上传应用文件

#### 方法一：Git Clone（推荐）
```bash
# 克隆代码（替换为你的仓库地址）
git clone https://github.com/liangruchao/news-subscription-app.git .

# 或使用 SSH
git clone git@github.com:liangruchao/news-subscription-app.git .
```

#### 方法二：手动上传
```bash
# 在本地执行
cd /path/to/project
tar -czf news-app.tar.gz \
    backend/target/*.jar \
    database/init.sql \
    frontend/dist/

# 上传到 VPS
scp news-app.tar.gz root@your_vps_ip:/var/www/news-app/

# 在 VPS 上解压
cd /var/www/news-app
tar -xzf news-app.tar.gz
```

### 6.3 构建后端应用
```bash
# 进入后端目录
cd /var/www/news-app/backend

# 安装 Maven（如果没有）
apt install -y maven

# 构建项目
mvn clean package -DskipTests

# 或使用预构建的 JAR
# 确保 target/ 目录有 .jar 文件
```

### 6.4 配置应用配置文件
```bash
# 创建生产配置
vim /var/www/news-app/application-production.properties
```

```properties
# 服务器配置
server.port=8081

# 应用名称
spring.application.name=news-subscription-app

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/news_app?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=newsapp
spring.datasource.password=your_db_password_here
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Redis 配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=your_redis_password_here
spring.data.redis.database=0
spring.data.redis.timeout=3000ms

# NewsAPI 配置
newsapi.api-key=your_newsapi_key_here
newsapi.base-url=https://newsapi.org/v2
newsapi.page-size=10

# 日志配置
logging.level.com.newsapp=INFO
logging.level.org.springframework.web=WARN
logging.file.path=/var/log/news-app
```

### 6.5 初始化数据库
```bash
# 导入数据库脚本
mysql -u newsapp -p news_app < /var/www/news-app/database/init.sql
```

---

## 七、创建 Systemd 服务

### 7.1 创建服务文件
```bash
vim /etc/systemd/system/newsapp.service
```

```ini
[Unit]
Description=News Subscription Application
After=network.target mysql.service redis.service

[Service]
Type=simple
User=newsapp
Group=newsapp
WorkingDirectory=/var/www/news-app/backend
Environment="JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"
Environment="SPRING_PROFILES_ACTIVE=production"
ExecStart=/usr/lib/jvm/java-21-openjdk-amd64/bin/java -jar \
    -Dspring.profiles.active=production \
    -Dserver.port=8081 \
    -Xms512m -Xmx1024m \
    /var/www/news-app/backend/target/news-subscription-backend-*.jar
ExecStop=/bin/kill -15 $MAINPID
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=newsapp

[Install]
WantedBy=multi-user.target
```

### 7.2 启动服务
```bash
# 重载 systemd
systemctl daemon-reload

# 启动服务
systemctl start newsapp

# 设置开机自启
systemctl enable newsapp

# 检查状态
systemctl status newsapp

# 查看日志
journalctl -u newsapp -f
```

---

## 八、Nginx 反向代理配置

### 8.1 安装 Nginx
```bash
apt install -y nginx

# 启动 Nginx
systemctl start nginx
systemctl enable nginx
```

### 8.2 创建应用配置
```bash
vim /etc/nginx/sites-available/news-app
```

```nginx
# 后端 API 反向代理
server {
    listen 80;
    server_name api.yourdomain.com;  # 替换为你的域名

    # 日志
    access_log /var/log/nginx/news-app-access.log;
    error_log /var/log/nginx/news-app-error.log;

    # 反向代理到 Spring Boot
    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket 支持
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        # 超时配置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 限制请求体大小
    client_max_body_size 10M;
}

# 前端静态文件（如果需要）
server {
    listen 80;
    server_name www.yourdomain.com yourdomain.com;

    root /var/www/news-app/frontend/dist;
    index index.html;

    # 前端路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 8.3 启用配置
```bash
# 创建符号链接
ln -s /etc/nginx/sites-available/news-app /etc/nginx/sites-enabled/

# 测试配置
nginx -t

# 重载 Nginx
systemctl reload nginx
```

---

## 九、防火墙配置

### 9.1 配置 UFW
```bash
# 默认策略
ufw default deny incoming
ufw default allow outgoing

# 允许 SSH
ufw allow 22/tcp

# 允许 HTTP/HTTPS
ufw allow 80/tcp
ufw allow 443/tcp

# 启用防火墙
ufw enable

# 查看状态
ufw status numbered
```

### 9.2 禁用 ping（可选）
```bash
# 编辑配置
vim /etc/ufw/before.rules

# 在 *filter 部分后添加：
-A ufw-before-input -p icmp --icmp-type echo-request -j DROP

# 重载
ufw reload
```

---

## 十、SSL 证书配置（推荐）

### 10.1 安装 Certbot
```bash
apt install -y certbot python3-certbot-nginx
```

### 10.2 获取证书
```bash
# 自动配置 Nginx
certbot --nginx -d api.yourdomain.com -d www.yourdomain.com

# 按提示输入邮箱并同意条款
```

### 10.3 自动续期
```bash
# 测试续期
certbot renew --dry-run

# 续期已自动配置在 cron 中
# 查看定时任务
systemctl list-timers | grep certbot
```

---

## 十一、监控和日志

### 11.1 日志管理
```bash
# 创建日志目录
mkdir -p /var/log/news-app
chown newsapp:newsapp /var/log/news-app

# 配置 logrotate
vim /etc/logrotate.d/newsapp
```

```
/var/log/news-app/*.log {
    daily
    rotate 14
    compress
    delaycompress
    notifempty
    create 0640 newsapp newsapp
    sharedscripts
}
```

### 11.2 系统监控
```bash
# 安装 htop
apt install -y htop

# 查看系统资源
htop

# 查看服务状态
systemctl status newsapp mysql redis-server nginx
```

---

## 十二、部署验证

### 12.1 检查服务状态
```bash
# 检查所有服务
systemctl status newsapp
systemctl status mysql
systemctl status redis-server
systemctl status nginx

# 检查端口监听
ss -tlnp | grep -E '80|443|8081|3306|6379'
```

### 12.2 测试 API
```bash
# 测试后端 API
curl http://localhost:8081/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"test","email":"test@example.com","password":"Test123"}'

# 测试 Nginx 代理
curl http://your_vps_ip/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"test2","email":"test2@example.com","password":"Test123"}'
```

### 12.3 查看日志
```bash
# 应用日志
journalctl -u newsapp -f

# Nginx 日志
tail -f /var/log/nginx/news-app-access.log
tail -f /var/log/nginx/news-app-error.log

# MySQL 日志
tail -f /var/log/mysql/error.log
```

---

## 十三、备份策略

### 13.1 数据库备份
```bash
# 创建备份目录
mkdir -p /var/backups/news-app

# 创建备份脚本
vim /usr/local/bin/backup-news-app.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/var/backups/news-app"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="news_app"
DB_USER="newsapp"
DB_PASS="your_db_password"

# 备份数据库
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME | gzip > $BACKUP_DIR/db_$DATE.sql.gz

# 删除 7 天前的备份
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete

echo "Backup completed: db_$DATE.sql.gz"
```

```bash
# 设置执行权限
chmod +x /usr/local/bin/backup-news-app.sh

# 添加定时任务（每天凌晨 2 点）
crontab -e
```

```
0 2 * * * /usr/local/bin/backup-news-app.sh >> /var/log/news-app/backup.log 2>&1
```

### 13.2 应用文件备份
```bash
# 备份应用配置
tar -czf /var/backups/news-app/config_$(date +%Y%m%d).tar.gz \
    /var/www/news-app/application-production.properties
```

---

## 十四、故障排查

### 14.1 常见问题

#### 服务无法启动
```bash
# 查看详细日志
journalctl -u newsapp -n 50 --no-pager

# 检查端口占用
ss -tlnp | grep 8081

# 检查 Java 版本
java -version
```

#### 数据库连接失败
```bash
# 测试连接
mysql -u newsapp -p news_app

# 检查 MySQL 状态
systemctl status mysql

# 查看错误日志
tail -f /var/log/mysql/error.log
```

#### Redis 连接失败
```bash
# 测试连接
redis-cli -a your_password ping

# 检查 Redis 状态
systemctl status redis-server
```

### 14.2 性能优化
```bash
# 查看系统负载
top
htop

# 查看磁盘 I/O
iostat -x 1

# 查看网络连接
ss -s
```

---

## 十五、安全加固

### 15.1 SSH 安全
```bash
# 编辑 SSH 配置
vim /etc/ssh/sshd_config

# 推荐配置：
PermitRootLogin no
PasswordAuthentication no  # 使用密钥登录
PubkeyAuthentication yes
Port 22222  # 修改默认端口

# 重启 SSH
systemctl restart sshd
```

### 15.2 Fail2Ban 防护
```bash
# 安装 Fail2Ban
apt install -y fail2ban

# 启动服务
systemctl start fail2ban
systemctl enable fail2ban
```

---

## 检查清单

部署完成后，请确认以下项目：

- [ ] Java 21 已安装并配置 JAVA_HOME
- [ ] MySQL 8.0 已安装，数据库和用户已创建
- [ ] Redis 已安装并配置密码
- [ ] 应用文件已上传并构建
- [ ] Systemd 服务已创建并启动
- [ ] Nginx 反向代理已配置
- [ ] 防火墙规则已配置
- [ ] SSL 证书已安装（推荐）
- [ ] 数据库备份脚本已配置
- [ ] 日志轮转已配置
- [ ] 服务监控已配置
- [ ] API 接口测试通过

---

**下一步**: 完成配置后，进行压力测试验证性能提升效果。
