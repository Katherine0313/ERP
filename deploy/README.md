# ERP 生产部署

## 目录

```
deploy/
├── Dockerfile              # ERP 后端镜像（构建上下文是 target/）
├── docker-compose.yml      # 生产编排：erp-boot + erp-redis
├── .env.example            # 环境变量模板（复制成 .env 后填真实值）
├── release.sh              # 后端发布：构建 → 推送 → 服务器拉取 → 重启
├── release-web.sh          # 前端发布：npm build → 上传 → rsync 到站点目录
├── run-sql-prod.sh         # 在服务器上执行 SQL 文件
├── nginx-erp.conf          # 宝塔站点配置参考（location 块）
└── sql/                    # 数据库变更脚本，按日期命名
```

## 一、首次部署（一次性）

### 1. 准备 TCR 镜像仓库

仓库已建好：

```
ccr.ccs.tencentyun.com/erp-boot/erp
```

`REGISTRY` 填这个**完整路径**（含命名空间和仓库名），`release.sh` 不会再往后面拼服务名。

> 服务器和本地都已登录 `ccr.ccs.tencentyun.com`，凭证对整个域名有效，不需要为新仓库单独登录。
>
> 同一个 TCR 账号下还有电商项目的 `jiemi-ecommerce` 命名空间（一堆镜像）。
> **不会混**：镜像靠完整路径区分，且 ERP 的 tag 统一带 `erp-` 前缀，
> `release.sh --rollback` 也只接受 `erp-` 前缀的 tag，防止误拉电商镜像。

### 2. 准备环境变量

```bash
cd deploy
cp .env.example .env
```

填入真实值：

| 变量 | 值 |
|---|---|
| `DB_USERNAME` | `erp_db` |
| `DB_PASSWORD` | 宝塔面板里 erp_db 的密码 |
| `JWT_SECRET` | `openssl rand -base64 48` 生成，至少 64 位 |
| `REDIS_PASSWORD` | 自定，与 docker-compose 里 redis 服务一致 |
| `REGISTRY` | `ccr.ccs.tencentyun.com/erp-boot/erp` |

### 3. 上传部署文件到服务器

```bash
scp deploy/docker-compose.yml deploy/Dockerfile ecommerce:/www/java/erp-boot/
scp deploy/.env.example ecommerce:/www/java/erp-boot/
ssh ecommerce "cd /www/java/erp-boot && cp .env.example .env"
```

然后编辑服务器上的 `.env` 填真实值：

```bash
ssh ecommerce "vi /www/java/erp-boot/.env"
```

### 4. 初始化数据库

```bash
# 本地
scp doc/sql/erp_db_init.sql deploy/sql/ ecommerce:/www/java/erp-boot/sql/
ssh ecommerce "bash /www/java/erp-boot/run-sql-prod.sh /www/java/erp-boot/sql/erp_db_init.sql"
```

### 5. 发布后端

```bash
./deploy/release.sh
```

### 6. 发布前端

```bash
./deploy/release-web.sh
```

### 7. 配 nginx

宝塔面板 → 网站 → `erp.jiemids.com` → 配置文件，
把 `nginx-erp.conf` 里的内容合并进去，注意：
- 证书路径要改成宝塔实际申请的路径
- 站点根目录 `/www/wwwroot/erp.jiemids.com` 要保证存在且属主是 `www`

### 8. 申请 SSL 证书

宝塔 → `erp.jiemids.com` → SSL → Let's Encrypt。
**注意**：域名 `jiemids.com` 的 NS 被 EdgeOne 接管，DNS 解析要在 **EdgeOne 控制台**
的「DNS 记录」页签里配置，不是 DNSPod 面板。

## 二、日常发布

```bash
# 后端
./deploy/release.sh              # 构建 + 推送 + 部署
./deploy/release.sh --dry-run    # 只打印命令
./deploy/release.sh --no-build   # 跳过打包，用现有 jar
./deploy/release.sh --rollback erp-20261126-abc1234

# 前端
./deploy/release-web.sh
./deploy/release-web.sh --rollback
```

TAG 默认格式 `erp-日期-git短SHA`（工作区有改动会带 `-dirty`）。
`erp-` 前缀是为了和同账号下电商项目的镜像区分，回滚时也只接受这个前缀的 tag。

## 三、服务器现状

| 项 | 值 |
|---|---|
| 目录 | `/www/java/erp-boot` |
| SSH | `ssh ecommerce`（root@193.112.176.228） |
| 后端端口 | `127.0.0.1:8082`（只对宿主机 nginx 可见） |
| Redis | 容器 `erp-redis`，宿主机 `127.0.0.1:16380` |
| MySQL | 宝塔宿主机实例 5.7.44，库 `erp_db` |
| 站点 | `/www/wwwroot/erp.jiemids.com` |

查看状态：

```bash
ssh ecommerce "docker ps --filter name=erp"
ssh ecommerce "docker logs --tail 100 erp-boot"
ssh ecommerce "cd /www/java/erp-boot && docker compose ps"
```

## 四、注意事项

1. **MySQL socket 已迁到 `/run/mysqld/mysqld.sock`** —— 宝塔默认把它配在 `/tmp/mysql.sock`，
   该文件被清理后，宝塔面板会爆红「数据库连接失败」，且所有走 socket 的客户端报 ERROR 2002。
   现在 socket 在 `/run`（tmpfs）里，不会再被按年龄清理；宝塔会自动跟随 `/etc/my.cnf` 的路径。
   若又出现面板爆红，检查 `/run/mysqld/mysqld.sock` 是否存在。

2. **建库必须走宝塔面板 API** —— 手工 `CREATE DATABASE` 建的库不在面板列表里，无法在面板管理。

3. **服务器内存只有 7.5G**，电商容器已占 4.2G。ERP 容器堆内存上限 512m，
   不要在服务器上编译（本地构建、服务器只 pull）。

4. **备份文件名不要带时间戳** —— 避免备份越积越多。数据库备份用固定文件名 + 覆盖。

5. **Docker 网络独立** —— ERP 用 `erp-net`，不挤进电商的 `java_my-net`。
