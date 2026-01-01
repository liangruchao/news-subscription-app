#!/bin/bash

# ============================================================
# Staging 环境 SSL 证书配置脚本
# 用途: 自动配置 Let's Encrypt SSL 证书
# ============================================================

set -e

DOMAIN="staging.lrc002.click"
EMAIL="admin@lrc002.click"  # 修改为你的邮箱

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_section() {
    echo ""
    echo "=================================================================="
    echo "  $1"
    echo "=================================================================="
    echo ""
}

# ============================================================
print_section "Staging 环境 SSL 配置"

# ============================================================
# 1. 检查 DNS 解析
# ============================================================
print_section "1. 检查 DNS 解析"

log_info "检查 $DOMAIN 的 DNS 解析..."
if nslookup $DOMAIN | grep "Address:" > /dev/null; then
    IP=$(nslookup $DOMAIN | grep "Address:" | tail -1 | awk '{print $2}')
    log_success "DNS 解析成功: $DOMAIN → $IP"

    # 检查是否指向正确的 VPS
    CURRENT_VPS_IP=$(curl -s ifconfig.me)
    if [ "$IP" = "$CURRENT_VPS_IP" ]; then
        log_success "DNS 指向当前 VPS"
    else
        log_warning "DNS 指向 $IP，当前 VPS 是 $CURRENT_VPS_IP"
        log_warning "请确保 Cloudflare DNS 已正确配置"
    fi
else
    log_error "DNS 解析失败"
    log_error "请先在 Cloudflare 配置 DNS:"
    log_error "  类型: A"
    log_error "  名称: staging"
    log_error "  内容: 47.103.204.114"
    exit 1
fi

# ============================================================
# 2. 安装 Certbot
# ============================================================
print_section "2. 安装 Certbot"

if ! command -v certbot &> /dev/null; then
    log_info "安装 Certbot..."
    sudo apt-get update -qq
    sudo apt-get install -y certbot python3-certbot-nginx
    log_success "Certbot 安装完成"
else
    log_success "Certbot 已安装"
fi

# ============================================================
# 3. 停止 Docker Nginx（端口 80 冲突）
# ============================================================
print_section "3. 释放端口 80"

log_info "停止 Docker Nginx..."
cd /var/www/news-app
docker compose stop nginx 2>/dev/null || true

# 确保端口 80 可用
while sudo netstat -tlnp | grep :80 > /dev/null; do
    log_warning "端口 80 仍被占用，等待..."
    sleep 2
done

log_success "端口 80 已释放"

# ============================================================
# 4. 申请 SSL 证书
# ============================================================
print_section "4. 申请 SSL 证书"

log_info "为 $DOMAIN 申请 SSL 证书..."
sudo certbot certonly --standalone \
    -d $DOMAIN \
    --email $EMAIL \
    --agree-tos \
    --no-eff-email \
    --non-interactive

if [ $? -eq 0 ]; then
    log_success "SSL 证书申请成功"
else
    log_error "SSL 证书申请失败"
    exit 1
fi

# ============================================================
# 5. 配置自动续期
# ============================================================
print_section "5. 配置自动续期"

log_info "测试证书续期..."
sudo certbot renew --dry-run

log_success "自动续期配置完成"

# ============================================================
# 6. 更新 Nginx 配置
# ============================================================
print_section "6. 更新 Nginx 配置"

log_info "创建 Nginx SSL 配置..."

# 创建新的 Nginx 配置
sudo tee /var/www/news-app/nginx/conf.d/default.conf > /dev/null << EOF
server {
    listen 80;
    server_name $DOMAIN;

    # Let's Encrypt 验证路径
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    # 重定向到 HTTPS
    return 301 https://\$server_name\$request_uri;
}

server {
    listen 443 ssl http2;
    server_name $DOMAIN;

    # SSL 证书
    ssl_certificate /etc/letsencrypt/live/$DOMAIN/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$DOMAIN/privkey.pem;

    # SSL 配置
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # 安全头部
    add_header Strict-Transport-Security "max-age=31536000" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;

    # 前端
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files \$uri \$uri/ /index.html;
    }

    # 后端 API
    location /api/ {
        proxy_pass http://localhost:8081;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        # CORS 支持
        add_header Access-Control-Allow-Origin * always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        if (\$request_method = 'OPTIONS') {
            return 204;
        }
    }

    # WebSocket 支持
    location /ws {
        proxy_pass http://localhost:8081;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 健康检查
    location /health {
        proxy_pass http://localhost:8081/api/auth/current;
        access_log off;
    }
}
EOF

log_success "Nginx 配置已更新"

# ============================================================
# 7. 重启 Docker Nginx
# ============================================================
print_section "7. 重启服务"

log_info "重启 Docker Nginx..."
docker compose up -d nginx

sleep 3

# ============================================================
# 8. 验证配置
# ============================================================
print_section "8. 验证配置"

log_info "检查 HTTPS 连接..."
if curl -s -I https://$DOMAIN | grep "HTTP" > /dev/null; then
    log_success "✓ HTTPS 配置成功"
    echo ""
    echo "访问地址: https://$DOMAIN"
else
    log_warning "HTTPS 验证失败，请检查配置"
fi

# ============================================================
print_section "配置完成"

log_success "Staging 环境 SSL 配置完成！"
echo ""
echo "证书信息:"
sudo certbot certificates
echo ""
echo "访问地址:"
echo "  HTTPS: https://$DOMAIN"
echo "  HTTP:  http://$DOMAIN (自动重定向到 HTTPS)"
echo ""
echo "证书自动续期已启用"
echo ""
