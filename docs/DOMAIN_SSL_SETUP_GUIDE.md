# 域名和 HTTPS 配置指南

**域名**: lrc002.click
**DNS 管理**: Cloudflare

---

## DNS 解析配置

### 1. Cloudflare DNS 设置

登录 Cloudflare → 选择 lrc002.click → DNS → Records

添加以下记录：

| 类型 | 名称 | 内容 | 代理状态 |
|------|------|------|----------|
| A | @ | 139.224.189.183 | 仅 DNS (灰色云) |
| A | www | 139.224.189.183 | 仅 DNS (灰色云) |
| A | staging | 47.103.204.114 | 仅 DNS (灰色云) |

**注意**: 先设置为"仅 DNS"（灰色云），SSL 证书配置好后再改为"代理"（橙色云）

---

## SSL 证书配置

### Staging 环境 (staging.lrc002.click)

```bash
# SSH 登录 Staging VPS
ssh root@47.103.204.114

# 1. 安装 Certbot
sudo apt-get update
sudo apt-get install -y certbot python3-certbot-nginx

# 2. 申请 SSL 证书
sudo certbot --nginx -d staging.lrc002.click

# 3. 验证自动续期
sudo certbot renew --dry-run

# 4. 查看 Nginx 配置
cat /etc/nginx/sites-available/staging.lrc002.click
```

### Production 环境 (lrc002.click)

```bash
# SSH 登录 Production VPS
ssh root@139.224.189.183

# 1. 安装 Certbot
sudo apt-get update
sudo apt-get install -y certbot

# 2. 独立模式申请证书（端口 80 需要可用）
sudo certbot certonly --standalone -d lrc002.click -d www.lrc002.click

# 3. 配置自动续期
sudo certbot renew --dry-run
```

---

## Nginx 配置

### Staging Nginx 配置

`/var/www/news-app/nginx/conf.d/default.conf`:

```nginx
server {
    listen 80;
    server_name staging.lrc002.click;

    # Let's Encrypt 验证路径
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name staging.lrc002.click;

    # SSL 证书
    ssl_certificate /etc/letsencrypt/live/staging.lrc002.click/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/staging.lrc002.click/privkey.pem;

    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # 前端
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 支持
    location /ws {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### Production Nginx 配置

由于 Production 使用 jar 运行，需要安装并配置 Nginx：

```bash
# 1. 安装 Nginx
sudo apt-get install -y nginx

# 2. 创建配置文件
sudo tee /etc/nginx/sites-available/lrc002.click > /dev/null << 'EOF'
server {
    listen 80;
    server_name lrc002.click www.lrc002.click;

    # Let's Encrypt 验证路径
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name lrc002.click www.lrc002.click;

    # SSL 证书
    ssl_certificate /etc/letsencrypt/live/lrc002.click/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/lrc002.click/privkey.pem;

    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # HSTS
    add_header Strict-Transport-Security "max-age=31536000" always;

    # 前端（如果有）
    location / {
        root /var/www/news-app/news-subscription-app/frontend/public;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 支持
    location /ws {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

# 3. 启用配置
sudo ln -s /etc/nginx/sites-available/lrc002.click /etc/nginx/sites-enabled/

# 4. 删除默认配置
sudo rm -f /etc/nginx/sites-enabled/default

# 5. 测试配置
sudo nginx -t

# 6. 重启 Nginx
sudo systemctl restart nginx
```

---

## 配置步骤总结

### 第一步：配置 Cloudflare DNS

1. 登录 Cloudflare
2. 添加 A 记录：
   - `@` → `139.224.189.183` (Production)
   - `www` → `139.224.189.183` (Production)
   - `staging` → `47.103.204.114` (Staging)
3. 等待 DNS 生效（通常几分钟）

### 第二步：配置 Staging SSL

```bash
ssh root@47.103.204.114
# 运行 SSL 配置脚本
bash /var/www/news-app/news-subscription-app/scripts/setup-ssl-staging.sh
```

### 第三步：配置 Production SSL

```bash
ssh root@139.224.189.183
# 运行 SSL 配置脚本
bash /var/www/news-app/news-subscription-app/scripts/setup-ssl-production.sh
```

### 第四步：更新 Cloudflare SSL 模式

DNS 验证通过后：
1. 在 Cloudflare 中将记录改为"代理"（橙色云）
2. SSL/TLS 设置 → Full 模式

---

## 验证配置

```bash
# 检查 SSL 证书
curl -I https://lrc002.click
curl -I https://staging.lrc002.click

# 检查 SSL 评级
# 访问 https://www.ssllabs.com/ssltest/
```

---

## 自动续期

Certbot 会自动配置续期任务。验证：

```bash
# 查看定时任务
sudo systemctl list-timers | grep certbot

# 手动测试续期
sudo certbot renew --dry-run
```

---

## 故障排查

### 证书申请失败

```bash
# 检查端口 80 是否被占用
sudo netstat -tlnp | grep :80

# 检查 Nginx 状态
sudo systemctl status nginx

# 查看 Certbot 日志
sudo cat /var/log/letsencrypt/letsencrypt.log
```

### HTTPS 无法访问

```bash
# 检查防火墙
sudo ufw status
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# 检查 Nginx 配置
sudo nginx -t
```

---

## 访问地址

配置完成后：

| 环境 | URL |
|------|-----|
| Production | https://lrc002.click |
| Production | https://www.lrc002.click |
| Staging | https://staging.lrc002.click |
