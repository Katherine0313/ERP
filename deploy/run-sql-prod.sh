#!/usr/bin/env bash
# 在生产服务器上执行 SQL 文件：bash run-sql-prod.sh <sql文件路径>
# 读取 /www/java/erp-boot/.env 的数据库账号，自动连本机 MySQL。
#
# 用 TCP 连 127.0.0.1：宝塔默认把 socket 配在 /tmp/mysql.sock，
# 该文件可能被系统清理掉，导致走 socket 的客户端全部报 ERROR 2002。
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "用法: bash run-sql-prod.sh <sql文件路径>"
  exit 1
fi

cd /www/java/erp-boot
set -a
. ./.env
set +a

mysql --protocol=TCP -h127.0.0.1 -u"$DB_USERNAME" -p"$DB_PASSWORD" \
      --default-character-set=utf8mb4 erp_db -e "source $1"
