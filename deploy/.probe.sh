echo '############ 1. ERP 相关文件的全部位置 ############'
echo ''
echo '--- A. nginx 站点配置（宝塔）---'
ls -la /www/server/panel/vhost/nginx/erp.jiemids.com.conf*
echo ''
echo '--- B. SSL 证书 ---'
ls -la /www/server/panel/vhost/cert/erp.jiemids.com/
echo ''
echo '--- C. 前端静态文件 ---'
ls -la /www/wwwroot/erp.jiemids.com/
echo ''
echo '--- D. 部署目录（后端容器配置）---'
ls -la /www/java/erp-boot/
echo ''
echo '--- E. SQL 脚本 ---'
ls -la /www/java/erp-boot/sql/
echo ''
echo '--- F. Docker 容器 ---'
docker ps --filter name=erp --format '{{.Names}}|{{.Status}}|{{.Ports}}'
echo ''
echo '--- G. 镜像 ---'
docker images --format '{{.Repository}}:{{.Tag}}' | grep erp
echo ''
echo '--- H. EdgeOne 的缓存刷新 ---'
echo '（在 EdgeOne 控制台，不在服务器上）'

echo ''
echo '############ 2. nginx 站点配置内容 ############'
cat /www/server/panel/vhost/nginx/erp.jiemids.com.conf

echo ''
echo '############ 3. 部署目录里的 .env（凭据）############'
cat /www/java/erp-boot/.env

echo ''
echo '############ 4. docker-compose.yml ############'
cat /www/java/erp-boot/docker-compose.yml
