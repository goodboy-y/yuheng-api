# 玉衡API

<p align="center">
  <strong>SQL 即 API —— 将 SQL 查询直接转换为 RESTful API 接口的低代码平台</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21+-orange.svg" alt="JDK 21+">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg" alt="Spring Boot 4.0">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License MIT">
  <img src="https://img.shields.io/badge/GraalVM-Native%20Image-purple.svg" alt="GraalVM">
</p>

## 简介

玉衡API 是一个低代码 API 生成平台，只需编写 SQL 语句即可自动生成 RESTful API 接口，无需编写 Controller、Service、DAO 代码。支持多数据源、插件扩展、访问控制、数据导出，适用于数据中台、报表系统、低代码平台等场景。

## 特性

- **SQL 转 API**：编写 SQL 查询，自动生成 RESTful API，支持 `#{param}` 占位符参数化查询
- **多数据源**：支持 MySQL、Oracle 等多种数据库，每个 API 独立绑定数据源
- **访问控制**：基于 ClientId/Secret 的两层鉴权机制，细粒度的 API 访问授权
- **插件系统**：可扩展的插件架构，内置分页插件，支持自定义请求/响应/SQL 处理
- **字段映射**：自动驼峰转换，支持自定义列名、Excel 导出列宽
- **数据导出**：查询结果一键导出为 Excel（.xlsx），自动应用字段映射
- **限流熔断**：基于 Resilience4j 的限流器和熔断器，保障服务高可用
- **GraalVM 支持**：支持编译为 Native Image，毫秒级启动
- **Java SDK**：提供 Java 客户端 SDK，快速集成到现有项目

## 环境要求

| 组件   | 版本        |
| ------ | ----------- |
| JDK    | 21+         |
| 数据库 | MySQL 8.0+ / Oracle 12c+ |
| Redis  | 7.0+（可选）|
| Maven  | 3.6+        |

## 快速开始

### 1. Docker 部署（推荐）

```shell
docker run -d --name=yuheng-api \
  -p 8520:8520 \
  -e SERVER_PORT=8520 \
  -e CONTEXT_PATH=/yuheng-api \
  -e SPRING_REDIS_HOST=your-redis-host \
  -e SPRING_REDIS_PASSWORD='your-redis-password' \
  -e SPRING_DATASOURCE_URL='jdbc:oracle:thin:@172.17.0.1:1521/orclpdb1' \
  -e SPRING_DATASOURCE_USERNAME=your-db-username \
  -e SPRING_DATASOURCE_PASSWORD=your-db-password \
  yuhengapi:1.0
```

### 2. 创建你的第一个 API

**步骤一：创建数据源**

![](/doc/images/datasource-add.png)

**步骤二：创建客户端**

![](/doc/images/client-add.png)

**步骤三：配置 API 接口**

![](/doc/images/api-add.png)

**步骤四：调用 API**

```shell
curl -X GET 'http://localhost:8520/yuheng-api/api/person?dept=技术部' \
  -H 'clientId: <your-client-id>' \
  -H 'secret: <your-secret>'
```

![](/doc/images/api-use.png)

## 使用示例

### 管理端操作

1. 创建数据源 → 连接目标业务数据库
2. 创建客户端 → 获取 clientId 和 secret
3. 配置 API → 编写 SQL 模板，定义参数
4. 授权访问 → 将 API 授权给客户端
5. 上线发布 → 切换 API 状态为上线

### 外部调用

**curl 调用：**

```shell
curl 'http://localhost:8520/yuheng-api/api/person?dept=技术部&page=1&pageSize=10' \
  -H 'clientId: w8hpmb7T1xbWmsGr' \
  -H 'secret: XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW'
```

**Java SDK 调用：**

```java
YuhengClient client = new YuhengClient(
    "http://localhost:8520/yuheng-api",
    "your-client-id",
    "your-secret"
);

// 查询数据
Result<List<Map<String, Object>>> result = client.queryData("/api/person");

// 分页查询
Result<List<Map<String, Object>>> page = client.queryPage("/api/person", null, 0, 10);
```

详细使用说明见 [客户端文档](./doc/usage.md)。

## SQL 模板语法

在 SQL 中使用 `#{paramName}` 作为参数占位符，系统自动进行参数化查询（防 SQL 注入）：

```sql
SELECT id, name, age, department
FROM person
WHERE department = #{dept}
  AND age > #{minAge}
ORDER BY id
```

配置参数类型后，HTTP 请求参数会自动映射：

```
GET /api/person?dept=技术部&minAge=25
```

## 项目结构

```
src/main/java/com/compass/yuhengapi/
├── YuhengApiApplication.java    # 启动类
├── common/                       # 公共模块（Result、分页、Excel 工具等）
├── conf/                         # 配置类（安全、跨域、插件初始化）
├── controller/                   # 控制器层
├── filter/                       # 过滤器（JWT 鉴权、API 客户端鉴权）
├── model/
│   ├── bean/                     # 内部 Bean（ApiSql、SqlParam）
│   ├── dto/                      # 数据传输对象
│   ├── entities/                 # JPA 实体
│   └── vo/                       # 视图对象
├── plugin/                       # 插件系统（Plugin 接口 + 内置实现）
├── repo/                         # JPA Repository
├── service/                      # 服务层
├── task/                         # 定时任务
└── util/                         # 工具类（JWT、JDBC、连接池管理）
```

## 技术栈

| 类别       | 技术                                                 |
| ---------- | ---------------------------------------------------- |
| 框架       | Spring Boot 4.0 + Spring Security + Spring Data JPA  |
| 数据库     | MySQL / Oracle                                       |
| 连接池     | Druid 1.1.20                                         |
| 缓存       | Caffeine + Redis                                     |
| 限流熔断   | Resilience4j 2.1.0                                   |
| SQL 解析   | JSqlParser 5.3                                       |
| JSON       | Fastjson2 2.0.40                                     |
| Excel      | Apache POI 5.2.5                                     |
| 工具库     | Hutool 5.8.40、Apache Commons、MapStruct 1.6.3        |
| 模板引擎   | Thymeleaf                                            |
| 原生编译   | GraalVM Native Image                                 |

## 文档

完整文档请访问 [在线文档](https://your-domain/yuheng-api/) 或本地运行：

```shell
npm install
npm run docs:dev
```

文档目录：

- **指南**：快速开始、安装部署、快速示例
- **功能**：数据源管理、API 配置、客户端管理、插件系统、字段映射、访问控制、数据导出
- **架构**：架构概览、数据库设计、安全设计
- **API 参考**：管理端 API、数据 API
- **SDK**：Java SDK 使用文档

## 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交代码 (`git commit -m 'feat: add amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 开发环境搭建

```shell
# 克隆项目
git clone <repository-url>
cd yuheng-api

# 配置数据库后启动
./mvnw spring-boot:run
```

## 许可证

本项目基于 [MIT License](./LICENSE) 发布。
