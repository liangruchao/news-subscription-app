#!/bin/bash

echo "=========================================="
echo "MySQL 密码重置 - 简化版"
echo "=========================================="
echo ""

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 步骤 1: 完全停止 MySQL
echo -e "${YELLOW}步骤 1/5: 停止所有 MySQL${NC}"
brew services stop mysql 2>/dev/null || true
sudo /usr/local/mysql/support-files/mysql.server stop 2>/dev/null || true
sudo killall -9 mysqld 2>/dev/null || true
sleep 2
echo -e "${GREEN}✓ 所有 MySQL 已停止${NC}"
echo ""

# 步骤 2: 启动安全模式（使用临时配置）
echo -e "${YELLOW}步骤 2/5: 启动安全模式${NC}"
# 创建临时配置，指定 socket 位置
mkdir -p /opt/homebrew/var/mysql

# 使用 --datadir 明确指定数据目录
mysqld_safe --skip-grant-tables --skip-networking --datadir=/opt/homebrew/var/mysql --log-error=/tmp/mysql_error.log &

echo "等待 MySQL 启动（8秒）..."
for i in {8..1}; do
    echo "  $i 秒..."
    sleep 1
done
echo ""

# 检查是否启动成功
if ps aux | grep mysqld_safe | grep -v grep > /dev/null; then
    echo -e "${GREEN}✓ 安全模式已启动${NC}"
else
    echo -e "${YELLOW}⚠ 安全模式可能未正确启动${NC}"
    echo "查看错误日志："
    cat /tmp/mysql_error.log | tail -20
fi
echo ""

# 步骤 3: 尝试多种方式连接
echo -e "${YELLOW}步骤 3/5: 重置密码${NC}"

# 方法 1: 使用 TCP 连接 127.0.0.1
echo "尝试方法 1: TCP 连接..."
if mysql -h 127.0.0.1 -P 3306 -u root 2>/dev/null <<EOF
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
FLUSH PRIVILEGES;
EOF
then
    echo -e "${GREEN}✓ 密码重置成功！${NC}"
else
    # 方法 2: 使用 socket
    echo "尝试方法 2: Socket 连接..."
    mysql --socket=/tmp/mysql.sock -u root 2>/dev/null <<EOF
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
FLUSH PRIVILEGES;
EOF
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 密码重置成功！${NC}"
    else
        # 方法 3: 尝试 localhost
        echo "尝试方法 3: Localhost 连接..."
        mysql -u root 2>/dev/null <<EOF
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';
FLUSH PRIVILEGES;
EOF
        if [ $? -ne 0 ]; then
            echo -e "${YELLOW}⚠ 无法连接到 MySQL${NC}"
            echo "请检查日志: /tmp/mysql_error.log"
            echo ""
            echo "手动方法："
            echo "1. 执行: mysql -u root"
            echo "2. 如果要求密码，尝试留空或输入常见密码"
            echo "3. 执行: ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123';"
        fi
    fi
fi
echo ""

# 步骤 4: 重启 MySQL
echo -e "${YELLOW}步骤 4/5: 重启 MySQL${NC}"
sudo killall mysqld 2>/dev/null || true
sleep 3
brew services start mysql
sleep 3
echo -e "${GREEN}✓ MySQL 已重启${NC}"
echo ""

# 步骤 5: 测试
echo -e "${YELLOW}步骤 5/5: 测试新密码${NC}"
if mysql -u root -proot123 -e "SELECT 'Success!' AS Status;" 2>/dev/null; then
    echo -e "${GREEN}✓ 密码验证成功！${NC}"
    echo ""
    echo "=========================================="
    echo -e "${GREEN}完成！${NC}"
    echo "=========================================="
    echo "MySQL 用户: root"
    echo "MySQL 密码: root123"
    echo ""
    echo "下一步：创建数据库"
    echo "mysql -u root -proot123 -e \"CREATE DATABASE news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES;\""
else
    echo -e "${YELLOW}⚠ 自动密码重置可能失败${NC}"
    echo "请尝试手动登录并设置密码"
fi
