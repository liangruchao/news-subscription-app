# ============================================================
# 多阶段构建 Dockerfile
# Stage 1: 构建阶段
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS builder

LABEL maintainer="news-app"
LABEL description="News Subscription Application Backend"

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 和源代码
COPY backend/pom.xml .
COPY backend/src ./src

# 构建应用 (跳过测试，因为测试在CI阶段已执行)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: 运行阶段
# ============================================================
FROM eclipse-temurin:21-jre-alpine

# 安装必要的工具
RUN apk add --no-cache curl tzdata

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户 (安全考虑)
RUN addgroup -S newsapp && adduser -S newsapp -G newsapp

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/news-app && \
    chown -R newsapp:newsapp /app /var/log/news-app

# 切换到非 root 用户
USER newsapp

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8081/api/auth/current || exit 1

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseG1GC -XX:+UseStringDeduplication"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
