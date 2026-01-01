# 测试环境说明文档

**更新日期**: 2026-01-01

---

## 测试执行摘要

### 当前测试状态

| 测试类型 | 测试数量 | 通过 | 失败 | 覆盖率 |
|----------|----------|------|------|--------|
| 单元测试 | 17 | 17 | 0 | - |
| 集成测试 | - | - | - | - |
| **总计** | 17 | 17 | 0 | 27% |

### 代码覆盖率

```
总体覆盖率: 27% (361/1307 指令)
分支覆盖率: 46% (12/26)
```

**模块覆盖率详情**:
- `UserService`: 56% (高)
- `SubscriptionService`: 56% (高)
- `Entity`: 63%
- `Controller`: 0% (需要 Java 21 环境)
- `Config`: 0% (需要 Java 21 环境)

---

## 测试文件清单

### 单元测试
- `UserServiceTest.java` - 用户服务测试（8个测试用例）
- `SubscriptionServiceTest.java` - 订阅服务测试（9个测试用例）

### 性能测试
- `performance-tests/jmeter/api-load-test.jmx` - JMeter 性能测试计划
- `performance-tests/jmeter/run-performance-test.sh` - 性能测试执行脚本
- `performance-tests/README.md` - 性能测试文档

---

## 环境要求

### Java 版本兼容性

**推荐环境**: Java 21

由于系统使用 Java 24/Java 23 与 Mockito/Byte Buddy 存在兼容性问题，集成测试（Controller 测试）需要在以下环境运行：

- **Java 21** (推荐) - 完全兼容
- **Java 17** (LTS) - 完全兼容
- **Java 11** (LTS) - 完全兼容（需要修改代码，去除 Text Blocks）

**不兼容**:
- Java 22+ - Mockito 无法 Mock 某些类

### 切换 Java 版本

**macOS**:
```bash
# 列出可用版本
/usr/libexec/java_home -V

# 使用 Java 21（如果已安装）
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 或使用 Homebrew 安装 Java 21
brew install openjdk@21
```

**Linux**:
```bash
# 使用 sdkman
sdk install java 21.0.1-tem
sdk use java 21.0.1-tem

# 或手动设置
export JAVA_HOME=/path/to/jdk-21
```

---

## 运行测试

### 单元测试（当前可用）

```bash
cd backend
mvn test
```

### 生成覆盖率报告

```bash
cd backend
mvn test jacoco:report
open target/site/jacoco/index.html
```

### 集成测试（需要 Java 21）

```bash
cd backend
mvn verify
```

### 性能测试

```bash
cd performance-tests/jmeter
./run-performance-test.sh
```

---

## 已知问题

### 1. Java 版本兼容性

**问题**: 系统 Java 版本过高（24/23）导致 Mockito 无法 Mock 类

**解决方案**:
- 切换到 Java 21 运行集成测试
- 单元测试不受影响

### 2. CSRF 保护

**问题**: 集成测试因 CSRF 保护返回 403

**解决方案**:
- 在测试配置中禁用 CSRF
- 或在测试请求中添加 CSRF Token

### 3. Text Blocks 语法

**问题**: Java 11 不支持 `"""` 文本块语法

**解决方案**:
- 使用 Java 21
- 或将文本块改为字符串拼接

---

## 后续计划

### 短期（1-2周）

1. **在 Java 21 环境运行集成测试**
   - AuthController 集成测试
   - SubscriptionController 集成测试
   - NewsController 集成测试

2. **修复 CSRF 测试问题**
   - 配置测试安全设置
   - 添加 CSRF Token 处理

### 中期（1个月）

1. **提升覆盖率到 60%**
   - 添加更多单元测试
   - 边界条件测试

2. **性能测试集成**
   - JMeter 负载测试
   - API 响应时间基准

---

## 参考文档

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 文档](https://javadoc.io/doc/org/mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JaCoCo 文档](https://www.jacoco.org/jacoco/trunk/doc/)
- [Spring Boot 测试](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
