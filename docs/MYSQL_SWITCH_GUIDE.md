# MySQL 配置解决方案

## 问题分析
您的系统同时有两个 MySQL：
1. **旧版 MySQL**: /usr/local/mysql (正在运行，占用端口 3306)
2. **新版 Homebrew MySQL**: /opt/homebrew (已停止)

## 解决方案（选择其一）

### 方案 1：使用旧版 MySQL（推荐，最简单）

如果您知道旧 MySQL 的 root 密码，直接使用它：

```bash
# 使用旧 MySQL 创建数据库
/usr/local/mysql/bin/mysql -u root -p

# 然后在 MySQL 命令行执行：
CREATE DATABASE news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SHOW DATABASES;
exit;

# 更新后端配置文件的密码
```

**更新配置文件**：
编辑 `backend/src/main/resources/application.properties`
将 `spring.datasource.password=root123` 改为您的实际密码

---

### 方案 2：切换到 Homebrew MySQL（彻底解决）

需要管理员权限来停止旧 MySQL：

```bash
# 1. 停止旧的 MySQL（需要输入 macOS 用户密码）
sudo /usr/local/mysql/support-files/mysql.server stop

# 2. 启动 Homebrew MySQL
brew services start mysql

# 3. 等待 3 秒
sleep 3

# 4. 设置密码并创建数据库
/opt/homebrew/opt/mysql/bin/mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123'; CREATE DATABASE news_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; SHOW DATABASES;"

# 5. 验证
mysql --version  # 应该显示 Homebrew 版本
```

---

### 方案 3：配置两个 MySQL 同时运行（高级）

让旧 MySQL 用 3306，Homebrew MySQL 用 3307：

```bash
# 修改 Homebrew MySQL 配置使用不同端口
# 保留旧的 MySQL 原样运行
# 配置应用连接到特定端口
```

---

## 快速诊断

运行此命令查看所有 MySQL 状态：
```bash
echo "=== 运行中的 MySQL ===" && ps aux | grep mysqld | grep -v grep && echo "" && echo "=== Homebrew 服务状态 ===" && brew services list | grep mysql
```

## 当前推荐

如果您是 MySQL 新手，**建议使用方案 1**（旧 MySQL），因为它已经在正常运行。

如果您想完全使用新安装的 Homebrew MySQL，使用**方案 2**。
