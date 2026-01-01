#!/bin/bash

set -e

echo "=========================================="
echo "重置 MySQL root 密码 - 完整版"
echo "=========================================="
echo ""

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 步骤 1: 停止所有 MySQL
echo -e "${YELLOW}步骤 1: 停止所有 MySQL 进程${NC}"
brew services stop mysql 2>/dev/null || true
sudo /usr/local/mysql/support-files/mysql.server stop 2>/dev/null || true
sudo killall -9 mysqld 2>/dev/null || true
echo "等待进程完全停止..."
sleep 3
echo -e "${GREEN}✓ 所有 MySQL 已停止${NC}"
echo ""

# 步骤 2: 验证所有 MySQL 已停止
echo -e "${YELLOW}步骤 2: 验证没有 MySQL 在运行${NC}"
if ps aux | grep -v grep | grep mysqld > /dev/null; then
    echo "警告：仍有 MySQL 进程在运行"
    ps aux | grep mysqld | grep -v grep
    echo "强制终止..."
    sudo killall -9 mysqld || true
    sleep 2
fi
echo -e "${GREEN}✓ 确认所有 MySQL 已停止${NC}"
echo ""

# 步骤 3: 以安全模式启动 Homebrew MySQL
echo -e "${YELLOW}步骤 3: 以安全模式启动 MySQL（跳过密码）${NC}"
mysqld_safe --skip-grant-tables --skip-networking --datadir=/opt/homebrew/var/mysql &
echo "等待 MySQL 启动..."
sleep 5
echo -e "${GREEN}✓ MySQL 安全模式已启动${NC}"
echo ""

# 步骤 4: 重置密码
echo -e "${YELLOW}步骤 4: 重置 root 密码${NC}"
mysql --socket=/opt/homebrew/var/mysql.sock -u root <<EOF
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 密码重置成功！新密码：root123${NC}"
else
    echo "密码重置失败，尝试其他方法..."
    mysql -u root -h 127.0.0.1 -P 3306 <<EOF
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
FLUSH PRIVILEGES;
EOF
fi
echo ""

# 步骤 5: 停止安全模式
echo -e "${YELLOW}步骤 5: 停止安全模式${NC}"
sudo killall mysqld || true
sleep 3
echo -e "${GREEN}✓ 安全模式已停止${NC}"
echo ""

# 步骤 6: 正常启动 MySQL
echo -e "${YELLOW}步骤 6: 正常启动 Homebrew MySQL${NC}"
brew services start mysql
sleep 3
echo -e "${GREEN}✓ MySQL 已正常启动${NC}"
echo ""

# 步骤 7: 测试新密码
echo -e "${YELLOW}步骤 7: 测试新密码并创建数据库${NC}"
if mysql -u root -proot123 -e "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 密码验证成功！${NC}"

    # 创建数据库
    mysql -u root -proot123 -e "CREATE DATABASE IF NOT EXISTS news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES;"
    echo ""
    echo "=========================================="
    echo -e "${GREEN}设置完成！${NC}"
    echo "=========================================="
    echo "MySQL 用户: root"
    echo "MySQL 密码: root123"
    echo "数据库: news_app"
    echo ""
else
    echo -e "${YELLOW}⚠  密码验证失败，可能需要手动配置${NC}"
fi