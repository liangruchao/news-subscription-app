#!/bin/bash

# News App Performance Test Script
# 使用 JMeter 运行 API 性能测试

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置
JMETER_VERSION="5.6.2"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="${SCRIPT_DIR}/results"
TEST_PLAN="${SCRIPT_DIR}/api-load-test.jmx"
BASE_URL="http://localhost:8081"

# 创建结果目录
mkdir -p "${RESULTS_DIR}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}News App Performance Test${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查 JMeter 是否安装
if ! command -v jmeter &> /dev/null; then
    echo -e "${YELLOW}JMeter 未找到，正在检查本地安装...${NC}"

    # 检查常见安装位置
    JMETER_PATHS=(
        "/opt/apache-jmeter-${JMETER_VERSION}/bin/jmeter"
        "/usr/local/jmeter/bin/jmeter"
        "$HOME/apache-jmeter-${JMETER_VERSION}/bin/jmeter"
    )

    JMETER_BIN=""
    for path in "${JMETER_PATHS[@]}"; do
        if [ -f "$path" ]; then
            JMETER_BIN="$path"
            break
        fi
    done

    if [ -z "$JMETER_BIN" ]; then
        echo -e "${RED}错误: 未找到 JMeter 安装${NC}"
        echo ""
        echo "请安装 JMeter:"
        echo "  1. 下载: https://jmeter.apache.org/download_jmeter.cgi"
        echo "  2. 解压到: /opt 或 ~/apache-jmeter-${JMETER_VERSION}"
        echo "  3. 或使用: brew install jmeter (macOS)"
        exit 1
    fi

    echo -e "${GREEN}找到 JMeter: ${JMETER_BIN}${NC}"
else
    JMETER_BIN="jmeter"
fi

# 检查后端服务是否运行
echo -e "${YELLOW}检查后端服务状态...${NC}"
if ! curl -s -f "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}警告: 后端服务未运行或健康检查失败${NC}"
    echo -e "请确保后端服务正在运行: ${BASE_URL}"
    echo ""
    read -p "是否继续运行测试? (y/N) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 运行测试
echo ""
echo -e "${GREEN}开始性能测试...${NC}"
echo "测试计划: ${TEST_PLAN}"
echo "目标URL: ${BASE_URL}"
echo "结果目录: ${RESULTS_DIR}"
echo ""

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULT_FILE="${RESULTS_DIR}/test-result-${TIMESTAMP}.jtl"
HTML_REPORT="${RESULTS_DIR}/html-report-${TIMESTAMP}"

# 运行 JMeter 非GUI模式
"$JMETER_BIN" -n \
    -t "${TEST_PLAN}" \
    -JBASE_URL="${BASE_URL}" \
    -l "${RESULT_FILE}" \
    -e -o "${HTML_REPORT}"

# 检查结果
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}测试完成!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "结果文件: ${RESULT_FILE}"
    echo "HTML报告: ${HTML_REPORT}/index.html"
    echo ""
    echo "在浏览器中打开报告:"
    echo "  open ${HTML_REPORT}/index.html"
else
    echo ""
    echo -e "${RED}测试失败，请查看日志${NC}"
    exit 1
fi
