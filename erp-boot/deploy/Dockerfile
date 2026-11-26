# ERP 后端镜像
# 构建上下文是 target/ 目录（里面只有一个 jar），所以这里用相对路径 COPY
FROM eclipse-temurin:17-jre-jammy

ENV TZ=Asia/Shanghai
WORKDIR /app

COPY *.jar app.jar

# 堆内存按服务器余量给：服务器 7.5G，电商容器已占 4.2G，ERP 给 512m 封顶
# -Duser.timezone 与 TZ 一起保证 LocalDateTime.now() 是北京时间
ENV JAVA_OPTS="-Xms128m -Xmx512m -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=64m -Xss256k -XX:+UseSerialGC -Duser.timezone=Asia/Shanghai"

# 固定 prod profile：application.yml 里 spring.profiles.default=dev，
# 不显式指定的话会回落到 dev（本地调试用的宽松配置：SQL 打印、异常外露）。
# 生产的 jar 里又剔除了 application-dev.yml，所以这里必须钉死 prod。
ENV SPRING_PROFILES_ACTIVE=prod

# exec 让 java 成为 PID 1，docker stop 能正常收到信号
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
