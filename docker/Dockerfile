# 智能面试评估平台 - Docker 构建文件
# 使用多阶段构建优化镜像体积

FROM eclipse-temurin:21-jre-alpine AS runtime

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建应用目录
WORKDIR /app

# 复制 JAR 文件（需先执行 mvn package）
# 各模块单独构建时替换为对应 JAR 名称
COPY target/*.jar app.jar

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 暴露端口（各模块端口不同，启动时覆盖）
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
