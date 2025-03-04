# 使用 OpenJDK 作为基础镜像
FROM openjdk:17-jdk-alpine

# 设置容器内的工作目录
WORKDIR /app

# 将本地的 Spring Boot jar 文件复制到容器内
COPY sky-server/target/sky-server-1.0-SNAPSHOT.jar /app/sky-server.jar

# 暴露容器的 8080 端口（Spring Boot 默认端口）
EXPOSE 8080

# 定义容器启动时的命令，启动 Spring Boot 应用
ENTRYPOINT ["java", "-jar", "sky-server.jar"]
