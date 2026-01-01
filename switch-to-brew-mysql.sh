#!/bin/bash

set -e  # 遇到错误立即退出

echo "=========================================="
echo "切换到 Homebrew MySQL"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 步骤 1: 检查旧 MySQL
echo -e "${YELLOW}步骤 1/6: 检查当前 MySQL 状态${NC}"
echo "当前运行的 MySQL 进程："
ps aux | grep mysqld | grep -v grep || echo "  (没有 MySQL 在运行)"
echo ""

# 步骤 2: 停止旧的 MySQL
echo -e "${YELLOW}步骤 2/6: 停止旧的 MySQL${NC}"
echo "需要管理员权限来停止旧的 MySQL..."
if sudo /usr/local/mysql/support-files/mysql.server stop 2>&1; then
    echo -e "${GREEN}✓ 旧 MySQL 已停止${NC}"
else
    echo -e "${YELLOW}⚠  旧 MySQL 可能已经停止或不存在${NC}"
fi
echo ""

# 步骤 3: 停止所有 Homebrew MySQL
echo -e "${YELLOW}步骤 3/6: 停止所有 Homebrew MySQL 服务${NC}"
brew services stop mysql 2>/dev/null || true
echo -e "${GREEN}✓ Homebrew MySQL 服务已停止${NC}"
echo ""

# 步骤 4: 启动 Homebrew MySQL
echo -e "${YELLOW}步骤 4/6: 启动 Homebrew MySQL${NC}"
brew services start mysql
echo -e "${GREEN}✓ Homebrew MySQL 已启动${NC}"
echo ""

# 步骤 5: 等待 MySQL 完全启动
echo -e "${YELLOW}步骤 5/6: 等待 MySQL 完全启动${NC}"
for i in {3..1}; do
    echo "  等待 $i 秒..."
    sleep 1
done
echo ""

# 步骤 6: 验证
echo -e "${YELLOW}步骤 6/6: 验证配置${NC}"
echo "当前 MySQL 版本："
MYSQL_VERSION=$(mysql --version 2>&1)
if echo "$MYSQL_VERSION" | grep -q "Homebrew"; then
    echo -e "${GREEN}✓ $MYSQL_VERSION${NC}"
    echo ""
    echo "=========================================="
    echo -e "${GREEN}切换成功！${NC}"
    echo "=========================================="
    echo ""
    echo "下一步：设置密码并创建数据库"
    echo ""
    echo "执行命令："
    echo "  mysql -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123'; CREATE DATABASE news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES;\""
    echo ""
else
    echo -e "${RED}✗ 切换可能未成功，当前版本：$MYSQL_VERSION${NC}"
    echo ""
    echo "请手动验证："
    echo "  mysql --version"
    echo "  ps aux | grep mysqld"
fi
