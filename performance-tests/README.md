# 性能测试文档

## 概述

本目录包含 News App 的性能测试脚本和配置，使用 Apache JMeter 进行 API 负载测试。

---

## 测试场景

### 1. 用户注册负载测试
- **目标**: 测试用户注册接口的并发处理能力
- **线程数**: 50
- **循环次数**: 10
- **总请求数**: 500
- **预期响应时间**: < 500ms

### 2. 登录负载测试
- **目标**: 测试登录接口的并发处理能力
- **线程数**: 100
- **循环次数**: 20
- **总请求数**: 2000
- **预期响应时间**: < 300ms

### 3. 获取新闻负载测试
- **目标**: 测试新闻获取接口的并发处理能力
- **线程数**: 200
- **循环次数**: 50
- **总请求数**: 10000
- **预期响应时间**: < 2000ms

---

## 环境要求

### 安装 JMeter

**macOS (Homebrew)**:
```bash
brew install jmeter
```

**Linux**:
```bash
# 下载
wget https://downloads.apache.org//jmeter/binaries/apache-jmeter-5.6.2.tgz

# 解压
tar -xzf apache-jmeter-5.6.2.tgz
sudo mv apache-jmeter-5.6.2 /opt/

# 创建符号链接
sudo ln -s /opt/apache-jmeter-5.6.2/bin/jmeter /usr/local/bin/jmeter
```

**Windows**:
1. 下载: https://jmeter.apache.org/download_jmeter.cgi
2. 解压到 `C:\Program Files\apache-jmeter-5.6.2`
3. 添加 `C:\Program Files\apache-jmeter-5.6.2\bin` 到 PATH

### 后端服务状态

确保后端服务正在运行:
```bash
cd backend
mvn spring-boot:run
```

---

## 运行测试

### 快速开始

```bash
cd performance-tests/jmeter
./run-performance-test.sh
```

### 手动运行

```bash
# 设置后端URL
export BASE_URL=http://localhost:8081

# 运行测试
jmeter -n -t api-load-test.jmx \
    -JBASE_URL=$BASE_URL \
    -l results/test-result.jtl \
    -e -o results/html-report
```

### GUI 模式 (用于调试)

```bash
jmeter -t api-load-test.jmx
```

---

## 测试报告

测试完成后，报告将保存在 `results/` 目录:

- **JTL 文件**: 原始测试数据 (`test-result-<timestamp>.jtl`)
- **HTML 报告**: 可视化测试报告 (`html-report-<timestamp>/index.html`)

### 查看报告

```bash
# macOS
open results/html-report-<timestamp>/index.html

# Linux
xdg-open results/html-report-<timestamp>/index.html

# Windows
start results/html-report-<timestamp>/index.html
```

---

## 性能指标

### 基准目标

| API 端点 | 响应时间 (P95) | 吞吐量 | 错误率 |
|----------|----------------|--------|--------|
| POST /api/auth/register | < 500ms | > 100 req/s | < 1% |
| POST /api/auth/login | < 300ms | > 200 req/s | < 1% |
| GET /api/news/category/{cat} | < 2000ms | > 50 req/s | < 5% |

### 监控指标

- **响应时间** (Response Time)
- **吞吐量** (Throughput)
- **错误率** (Error Rate)
- **并发用户数** (Concurrent Users)
- **CPU/Memory 使用率**

---

## 性能测试最佳实践

### 1. 测试前准备
- 确保测试环境与生产环境配置一致
- 准备足够的测试数据
- 清理测试数据库

### 2. 测试执行
- 从小负载开始逐步增加
- 监控服务器资源使用情况
- 记录每次测试结果

### 3. 结果分析
- 关注 P95 和 P99 响应时间
- 分析失败的请求
- 识别性能瓶颈

### 4. 持续优化
- 修复发现的性能问题
- 重新运行测试验证
- 建立性能基准

---

## 常见问题

### Q: JMeter 找不到命令
**A**: 确保已安装 JMeter 并将其添加到 PATH 环境变量

### Q: 测试失败 - 连接拒绝
**A**: 确保后端服务正在运行，端口配置正确

### Q: 响应时间过慢
**A**: 检查:
1. 后端服务日志
2. 数据库连接池配置
3. NewsAPI 调用是否正常

### Q: 如何修改测试参数
**A**: 编辑 `api-load-test.jmx` 文件，修改线程组配置:
- `ThreadGroup.num_threads`: 线程数
- `LoopController.loops`: 循环次数
- `ThreadGroup.ramp_time`: 启动时间(秒)

---

## 性能测试检查清单

### 测试前
- [ ] 后端服务已启动
- [ ] JMeter 已安装
- [ ] 数据库已初始化
- [ ] 清理旧测试数据

### 测试中
- [ ] 监控服务器资源
- [ ] 检查应用日志
- [ ] 观察错误率

### 测试后
- [ ] 保存测试报告
- [ ] 分析性能瓶颈
- [ ] 记录优化建议
- [ ] 更新性能基准

---

## 下一步

1. **添加更多测试场景**
   - 订阅管理负载测试
   - 用户新闻获取负载测试
   - 并发事务测试

2. **集成 CI/CD**
   - 自动运行性能测试
   - 性能回归检测

3. **建立监控**
   - 应用性能监控 (APM)
   - 实时性能仪表板

---

## 参考资源

- [Apache JMeter 官方文档](https://jmeter.apache.org/usermanual/index.html)
- [Spring Boot 性能优化指南](https://spring.io/guides/gs/performance/)
- [性能测试最佳实践](https://www.blazemeter.com/blog/blog/)
