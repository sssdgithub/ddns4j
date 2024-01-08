# 使用alpine作为基础镜像
FROM alpine:latest

# 指定作者信息
LABEL maintainer="15829762162@163.com"

# 设置工作目录
WORKDIR /usr/local/

# 暴露端口
EXPOSE 10000

# 使用openjdk 11作为基础镜像
FROM openjdk:11-jdk-slim

# 设置工作目录
WORKDIR /usr/local/

# 将您的Web JAR文件复制到容器中的指定位置
COPY ddns-v1.6.4-RELEASE.jar /usr/local/ddns-v1.6.4-RELEASE.jar

# 设置上海时区
RUN ln -snf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo Asia/Shanghai > /etc/timezone

# 设置入口点
CMD ["java", "-Xmx256m", "-Xms256m", "-jar", "ddns-v1.6.4-RELEASE.jar"]
