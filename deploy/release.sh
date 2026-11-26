#!/usr/bin/env bash
# ERP 后端发布脚本：本地构建镜像 → 推仓库 → 服务器拉取 → docker compose 重启。
#
# 设计要点：
#   1. 服务器只 pull 不 build（服务器内存偏紧，编译会把它压垮）
#   2. TAG 格式「erp-日期-git短SHA」，工作区有改动会带 -dirty 后缀
#      —— 带 erp- 前缀是为了和同账号下电商项目的镜像区分开，回滚也不会传错
#   3. --rollback <tag> 只拉旧镜像重启，不构建不推送；只接受 erp- 前缀的 tag
#   4. --dry-run 只打印命令不执行
#   5. 每次发布后把 compose 文件同步到服务器（服务器上的 compose 与本地保持一致）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SERVER="${DEPLOY_HOST:-ecommerce}"
REMOTE_BASE="${REMOTE_BASE:-/www/java/erp-boot}"
SERVICE="erp-boot"

NO_BUILD=0
NO_DEPLOY=0
DRY_RUN=0
ROLLBACK_TAG=""
SYNC_COMPOSE=1

usage() {
  cat <<'EOF'
用法:
  ./deploy/release.sh [选项]

选项:
  --no-build          跳过 Maven 打包（使用现有 target jar）
  --no-deploy         只构建/推送，不部署到服务器
  --no-sync-compose   不把本地 docker-compose.yml 同步到服务器
  --dry-run           只打印命令，不执行
  --rollback <tag>    回滚到指定 tag（不构建、不推送，只拉取并重启）
  -h, --help          显示帮助

环境变量:
  REGISTRY     完整镜像仓库路径（必填，或写在 deploy/.env）
               例如 ccr.ccs.tencentyun.com/erp-boot/erp
  TAG          镜像版本（默认：erp-日期-git短SHA）
  DEPLOY_HOST  SSH 别名或 user@host（默认 ecommerce）
  REMOTE_BASE  服务器部署目录（默认 /www/java/erp-boot）
  PLATFORM     可选，如 linux/amd64

示例:
  ./deploy/release.sh
  ./deploy/release.sh --dry-run
  ./deploy/release.sh --no-build
  ./deploy/release.sh --rollback erp-20261126-abc1234
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-build) NO_BUILD=1; shift ;;
    --no-deploy) NO_DEPLOY=1; shift ;;
    --no-sync-compose) SYNC_COMPOSE=0; shift ;;
    --dry-run) DRY_RUN=1; shift ;;
    --rollback)
      [[ $# -ge 2 && -n "$2" ]] || { echo "--rollback 需要指定 tag" >&2; exit 1; }
      ROLLBACK_TAG="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    -*) echo "未知选项: $1" >&2; usage; exit 1 ;;
    *) echo "未知参数: $1" >&2; usage; exit 1 ;;
  esac
done

# REGISTRY：优先环境变量，其次 deploy/.env
REGISTRY="${REGISTRY:-}"
if [[ -z "$REGISTRY" && -f "$ROOT/deploy/.env" ]]; then
  REGISTRY="$(grep -E '^REGISTRY=' "$ROOT/deploy/.env" | tail -n1 \
    | cut -d= -f2- | sed 's/\r$//' | tr -d '"' | tr -d "'")"
fi
if [[ -z "$REGISTRY" ]]; then
  echo "未配置 REGISTRY。请 export REGISTRY=... 或在 deploy/.env 写入 REGISTRY=..." >&2
  exit 1
fi
if [[ "$REGISTRY" == *"your-namespace"* ]]; then
  echo "REGISTRY 还是占位值，请换成 TCR 控制台里的完整仓库路径" >&2
  exit 1
fi
REGISTRY="${REGISTRY%/}"

# TAG
# 统一加 erp- 前缀：同一个 TCR 命名空间下还有电商项目的一堆镜像，
# 带前缀能在仓库列表里一眼分清归属，回滚时也不会传错 tag。
if [[ -n "$ROLLBACK_TAG" ]]; then
  TAG="$ROLLBACK_TAG"
  # 回滚只接受 erp- 前缀的 tag，防止误拉电商项目的镜像
  if [[ "$TAG" != erp-* ]]; then
    echo "回滚 tag 必须以 erp- 开头，收到: $TAG" >&2
    echo "（ERP 镜像的 tag 格式：erp-日期-git短SHA）" >&2
    exit 1
  fi
else
  if git -C "$ROOT" rev-parse --short HEAD >/dev/null 2>&1; then
    COMMIT="$(git -C "$ROOT" rev-parse --short HEAD)"
    if [[ -n "$(git -C "$ROOT" status --porcelain)" ]]; then
      COMMIT="${COMMIT}-dirty"
      echo "警告: 工作区有未提交改动，镜像 TAG 会带 -dirty" >&2
    fi
    DEFAULT_TAG="erp-$(date +%Y%m%d)-$COMMIT"
  else
    DEFAULT_TAG="erp-$(date +%Y%m%d%H%M%S)"
  fi
  TAG="${TAG:-$DEFAULT_TAG}"
fi

if [[ $DRY_RUN -eq 0 ]]; then
  if ! docker info >/dev/null 2>&1; then
    echo "Docker 未启动，请先启动 Docker Desktop" >&2
    exit 1
  fi
fi

run() {
  if [[ $DRY_RUN -eq 1 ]]; then
    printf '[dry-run] '
    printf '%q ' "$@"
    printf '\n'
  else
    "$@"
  fi
}

# REGISTRY 是完整仓库路径（含命名空间与仓库名），镜像名就是 REGISTRY:TAG
IMAGE="$REGISTRY:$TAG"

# ==================== 部署前检查 ====================
if [[ $DRY_RUN -eq 0 && $NO_DEPLOY -eq 0 ]]; then
  if ! ssh -o BatchMode=yes -o ConnectTimeout=8 "$SERVER" true 2>/tmp/erp_release_ssh_err.txt; then
    echo "SSH 连接失败：$SERVER" >&2
    sed 's/^/  /' /tmp/erp_release_ssh_err.txt >&2 || true
    echo "  请先确认：ssh $SERVER true 能免密登录" >&2
    exit 1
  fi
  # 服务器上 .env 必须存在，否则容器起来没有数据库密码
  if ! ssh -o BatchMode=yes -o ConnectTimeout=8 "$SERVER" \
      "test -f '$REMOTE_BASE/.env'" 2>/dev/null; then
    echo "服务器缺少 .env：$REMOTE_BASE/.env" >&2
    echo "  先把 deploy/.env.example 复制成 .env 填好真实值再上传" >&2
    exit 1
  fi
fi

# ==================== 构建 ====================
if [[ -z "$ROLLBACK_TAG" && $NO_BUILD -eq 0 ]]; then
  echo "==> Maven 打包（prod profile：剔除 application-dev.yml 里的本地凭据）"
  MVN="mvn"
  if [[ -x "$ROOT/mvnw" ]]; then
    MVN="$ROOT/mvnw"
  fi
  run "$MVN" -f "$ROOT/pom.xml" -Pprod clean package -DskipTests
fi

if [[ -z "$ROLLBACK_TAG" ]]; then
  CONTEXT="$ROOT/target"
  JAR="$(ls -1t "$CONTEXT"/"$SERVICE"-*.jar 2>/dev/null | head -n1 || true)"
  if [[ -z "$JAR" ]]; then
    echo "找不到 $CONTEXT/$SERVICE-*.jar，请先打包，或去掉 --no-build" >&2
    exit 1
  fi
  echo "==> 构建镜像 $IMAGE"
  BUILD_ARGS=(docker build -f "$ROOT/deploy/Dockerfile" -t "$IMAGE")
  if [[ -n "${PLATFORM:-}" ]]; then
    BUILD_ARGS+=(--platform "$PLATFORM")
  fi
  BUILD_ARGS+=("$CONTEXT")
  run "${BUILD_ARGS[@]}"
  echo "==> 推送 $IMAGE"
  run docker push "$IMAGE"
fi
if [[ $NO_DEPLOY -eq 1 ]]; then
  echo "==> 已跳过部署（--no-deploy）"
  exit 0
fi

# ==================== 部署 ====================
echo "==> 同步 compose 到服务器"
if [[ $SYNC_COMPOSE -eq 1 ]]; then
  if [[ $DRY_RUN -eq 1 ]]; then
    echo "[dry-run] scp deploy/docker-compose.yml $SERVER:$REMOTE_BASE/docker-compose.yml"
  else
    ssh "$SERVER" "mkdir -p '$REMOTE_BASE'"
    scp "$ROOT/deploy/docker-compose.yml" "$SERVER:$REMOTE_BASE/docker-compose.yml"
    # Dockerfile 也同步一份，服务器上万一要手工 build 用得上
    scp "$ROOT/deploy/Dockerfile" "$SERVER:$REMOTE_BASE/Dockerfile"
  fi
fi

echo "==> 部署 $SERVICE (TAG=$TAG)"
REMOTE_CMD="cd '$REMOTE_BASE' && docker pull '$IMAGE' && REGISTRY='$REGISTRY' TAG='$TAG' docker compose up -d --no-build --force-recreate '$SERVICE'"
run ssh "$SERVER" "$REMOTE_CMD"

if [[ $DRY_RUN -eq 0 ]]; then
  sleep 3
  STATUS="$(ssh "$SERVER" "docker inspect -f '{{.State.Status}}' '$SERVICE' 2>/dev/null" || true)"
  if [[ -n "$STATUS" ]]; then
    echo "    $SERVICE: $STATUS"
  else
    echo "    警告: 没查到 $SERVICE 容器，请到服务器执行 docker logs $SERVICE 查看" >&2
  fi
fi

echo "==> 完成：$IMAGE"
