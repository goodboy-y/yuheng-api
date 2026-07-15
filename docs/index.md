---
layout: home

hero:
  name: '玉衡API'
  text: 'SQL 即 API'
  tagline: 将 SQL 查询直接转换为 RESTful API 接口的低代码平台
  image:
    src: /logo.svg
    alt: 玉衡API
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 功能概览
      link: /features/

features:
  - icon: 🔌
    title: SQL 转 API
    details: 编写 SQL 查询，自动生成 RESTful API 接口，无需编写任何 Controller/Service 代码
  - icon: 🗄️
    title: 多数据源支持
    details: 支持 MySQL、Oracle 等多种数据库，可同时管理多个数据源
  - icon: 🔐
    title: 安全的访问控制
    details: 基于 ClientId/Secret 的 API 鉴权，支持细粒度的 API 访问授权
  - icon: 🧩
    title: 插件机制
    details: 灵活可扩展的插件系统，内置分页插件，支持自定义请求/响应/SQL 处理
  - icon: 📊
    title: 数据导出
    details: 支持 API 查询结果导出为 Excel，自动应用字段映射和列名别名
  - icon: 🛡️
    title: 高可用保障
    details: 内置限流、熔断机制，基于 Resilience4j 保障服务稳定性
  - icon: 📦
    title: Java SDK
    details: 提供 Java 客户端 SDK，快速集成到项目中调用 API
  - icon: 🔄
    title: GraalVM 支持
    details: 支持编译为 GraalVM Native Image，实现毫秒级启动和极低内存占用
---

## 核心流程

```mermaid
graph LR
    A[创建数据源] --> B[创建客户端]
    B --> C[配置 API 接口]
    C --> D[授权客户端访问]
    D --> E[调用 API]
```

## 技术栈

| 类别       | 技术                                      |
| ---------- | ----------------------------------------- |
| 框架       | Spring Boot 4.0 + Spring Security         |
| ORM        | Spring Data JPA + Hibernate               |
| 数据库     | MySQL / Oracle                            |
| 连接池     | Druid                                     |
| 缓存       | Caffeine + Redis                          |
| 限流熔断   | Resilience4j                              |
| SQL 解析   | JSqlParser                                |
| JSON       | Fastjson2                                 |
| 工具库     | Hutool, Apache Commons, MapStruct         |
| Excel      | Apache POI                               |
| 模板引擎   | Thymeleaf                                 |
| 原生编译   | GraalVM Native Image                      |
