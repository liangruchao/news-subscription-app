#!/bin/bash

# ============================================================
# Staging 环境快速检查脚本
# 在 Staging VPS 上运行此脚本
# ============================================================

echo "=================================================================="
echo "  Staging 环境检查"
echo "=================================================================="
echo ""

echo "1. Docker 状态:"
docker --version 2>/dev/null && echo "  ✓ Docker 已安装" || echo "  ✗ Docker 未安装"
docker compose version 2>/dev/null && echo "  ✓ Docker Compose 已安装" || echo "  ✗ Docker Compose 未安装"
echo ""

echo "2. GitHub SSH 连接测试:"
if timeout 10 ssh -o StrictHostKeyChecking=no -o ConnectTimeout=5 -T git@github.com 2>&1 | grep -q "successfully authenticated\|denied"; then
    echo "  ✓ GitHub SSH 可访问"
    GITHUB_OK=true
else
    echo "  ✗ GitHub SSH 无法访问（需要配置代理或密钥）"
    GITHUB_OK=false
fi
echo ""

echo "3. 部署目录检查:"
if [ -d "/var/www/news-app" ]; then
    echo "  ✓ 部署目录存在"
    if [ -f "/var/www/news-app/docker-compose.yml" ]; then
        echo "  ✓ docker-compose.yml 存在"
    else
        echo "  ✗ docker-compose.yml 不存在"
    fi
else
    echo "  ✗ 部署目录不存在"
fi
echo ""

echo "4. 容器状态:"
CONTAINER_COUNT=$(docker ps -a --format "{{.Names}}" 2>/dev/null | wc -l | tr -d ' ')
if [ "$CONTAINER_COUNT" = "0" ]; then
    echo "  ✗ 没有容器运行"
else
    echo "  ✓ 发现 $CONTAINER_COUNT 个容器"
    docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
fi
echo ""

echo "=================================================================="
echo "诊断结果:"
echo "=================================================================="
if [ "$GITHUB_OK" = true ]; then
    echo "✓ 环境正常，可以运行手动部署:"
    echo "  bash scripts/deploy-staging-manual.sh"
else
    echo "✗ 需要先配置 GitHub 访问:"
    echo "  1. 配置代理: bash scripts/configure-proxy-fixed.sh"
    echo "  2. 配置 SSH 密钥: bash scripts/configure-github-ssh.sh"
fi
echo ""
