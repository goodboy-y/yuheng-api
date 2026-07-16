# 玉衡API

<p align="center">
  <strong>SQL 即 API —— 将 SQL 查询直接转换为 RESTful API 接口的低代码平台</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21+-orange.svg" alt="JDK 21+">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg" alt="Spring Boot 4.0">
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License Apache 2.0">
</p>

## 简介

玉衡API 是一个低代码 API 生成平台，只需编写 SQL 语句即可自动生成 RESTful API 接口，无需编写 Controller、Service、DAO 代码。适用于数据中台、报表系统、低代码平台等场景。

## 快速开始

### Docker部署（推荐）

```shell
docker run -d --name=yuheng-api \
  -p 8520:8520 \
  -e SERVER_PORT=8520 \
  -e CONTEXT_PATH=/yuheng-api \
  -e SPRING_REDIS_HOST=your-redis-host \
  -e SPRING_REDIS_PASSWORD='your-redis-password' \
  -e SPRING_DATASOURCE_URL='jdbc:mysql://titi.vip:3306/yuheng?characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true' \
  -e SPRING_DATASOURCE_USERNAME=your-db-username \
  -e SPRING_DATASOURCE_PASSWORD=your-db-password \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
  registry.cn-hangzhou.aliyuncs.com/tianxuan/yuheng:latest
```

### Jar 包部署

```shell
./mvnw clean package -DskipTests
java -jar target/yuheng-api-0.0.1-SNAPSHOT.jar
```

## 文档

完整文档请访问 **[https://goodboy-y.github.io/yuheng-api/](https://goodboy-y.github.io/yuheng-api/)**，包含：

- 快速开始、安装部署、完整示例
- 数据源管理、API 配置、客户端管理、插件系统、字段映射、访问控制、数据导出
- 架构设计、数据库设计、安全设计
- 管理端 API 参考、数据 API 参考
- Java SDK 使用指南

也可以本地运行：

```shell
npm install
npm run docs:dev
```

## 许可证

本项目基于 [Apache License 2.0](./LICENSE) 发布。
