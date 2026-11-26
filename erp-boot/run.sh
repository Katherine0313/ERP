#!/usr/bin/env bash
# macOS / Linux 一键启动脚本
# 用法：./run.sh

set -e
cd "$(dirname "$0")"

# ---- 找 Java ----
if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="$(command -v java || true)"
fi
if [ -z "$JAVA" ]; then
    echo "[错误] 找不到 Java，请先安装 JDK 17"
    exit 1
fi

# ---- 找 Maven ----
if [ -x "./mvnw" ]; then
    MAVEN="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
    MAVEN="mvn"
else
    echo "[错误] 找不到 Maven"
    exit 1
fi

echo "========================================"
echo "  ERP-Boot 启动脚本"
echo "  Java:   $JAVA"
echo "  Maven:  $MAVEN"
echo "  端口:   8082"
echo "========================================"
echo

# ---- 核心：--add-opens 写死 ----
export JVM_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED"

echo "正在启动..."
exec "$MAVEN" spring-boot:run -Dspring-boot.run.jvmArguments="$JVM_OPTS"
