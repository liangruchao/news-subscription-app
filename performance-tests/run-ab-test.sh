#!/bin/bash

# News App Performance Test Script
# 使用 Apache Bench 进行负载测试

set -e

RESULTS_DIR="$(pwd)/results"
mkdir -p "$RESULTS_DIR"

echo "========================================="
echo "News App Performance Test"
echo "========================================="
echo ""
echo "Results will be saved to: $RESULTS_DIR"
echo ""

# 确保后端服务正在运行
if ! curl -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "Warning: Backend service may not be running"
    echo "Please ensure the backend is running on http://localhost:8081"
fi

# 测试数据文件
REGISTER_DATA='{"username":"testuser_'$(date +%s)'","email":"test'$(date +%s)'@example.com","password":"Password123"}'
LOGIN_DATA='{"username":"testuser","password":"Password123"}'

echo "$REGISTER_DATA" > /tmp/register.json
echo "$LOGIN_DATA" > /tmp/login.json

# 测试 1: 用户注册接口
echo "========================================="
echo "Test 1: User Registration Endpoint"
echo "========================================="
echo "Requests: 100"
echo "Concurrency: 10"
echo ""

ab -n 100 -c 10 \
   -p /tmp/register.json \
   -T application/json \
   http://localhost:8081/api/auth/register \
   | tee "$RESULTS_DIR/register-test.txt"

echo ""
echo "✓ Registration test completed"
echo ""

# 测试 2: 登录接口
echo "========================================="
echo "Test 2: Login Endpoint"
echo "========================================="
echo "Requests: 200"
echo "Concurrency: 20"
echo ""

ab -n 200 -c 20 \
   -p /tmp/login.json \
   -T application/json \
   http://localhost:8081/api/auth/login \
   | tee "$RESULTS_DIR/login-test.txt"

echo ""
echo "✓ Login test completed"
echo ""

# 测试 3: 获取新闻接口
echo "========================================="
echo "Test 3: Get News Endpoint"
echo "========================================="
echo "Requests: 500"
echo "Concurrency: 50"
echo ""

ab -n 500 -c 50 \
   http://localhost:8081/api/news/category/technology \
   | tee "$RESULTS_DIR/news-test.txt"

echo ""
echo "✓ News test completed"
echo ""

# 清理临时文件
rm -f /tmp/register.json /tmp/login.json

echo "========================================="
echo "Performance Test Summary"
echo "========================================="
echo ""
echo "Results saved to: $RESULTS_DIR"
echo ""
echo "To view detailed results:"
echo "  cat $RESULTS_DIR/register-test.txt"
echo "  cat $RESULTS_DIR/login-test.txt"
echo "  cat $RESULTS_DIR/news-test.txt"
echo ""
echo "Key metrics to look for:"
echo "  - Requests per second (higher is better)"
echo "  - Time per request (lower is better)"
echo "  - Failed requests (should be 0 or low)"
echo ""
