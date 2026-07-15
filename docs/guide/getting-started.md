# 快速开始

欢迎使用玉衡API！本指南将帮助你快速了解项目并开始使用。

## 什么是玉衡API？

玉衡API 是一个**低代码 API 生成平台**，核心功能是将 SQL 查询语句直接转换为 RESTful API 接口。你只需要：

1. 配置数据源
2. 编写 SQL 语句
3. 发布 API 接口

无需编写任何 Java Controller、Service 或 DAO 代码。

## 核心概念

### 数据源 (DataSource)

数据源定义了玉衡API需要连接的数据库信息，包括：

- 数据库类型（MySQL / Oracle）
- 连接地址
- 用户名和密码

一个玉衡API实例可以管理多个数据源。

### API 配置 (ApiConfig)

API 配置是核心实体，定义了：

- **名称**：API 的名称
- **路径**：API 的访问路径（如 `/api/person`）
- **SQL**：要执行的 SQL 语句，支持 `#{paramName}` 占位符
- **参数**：SQL 中占位符对应的请求参数定义
- **状态**：上线 / 下线

### 客户端 (Client)

客户端代表一个 API 调用方。每个客户端有唯一的 `clientId` 和 `secret`，调用 API 时需要在请求头中传入这些凭证。

### 插件 (Plugin)

插件系统允许在请求/响应/SQL 处理的各个阶段插入自定义逻辑。内置的分页插件可以为任意 API 自动添加分页功能。

### 字段映射 (FieldMapping)

字段映射定义了数据库字段与 API 返回字段之间的映射关系，用于：

- 字段重命名（如 `user_name` → `userName` 驼峰转换）
- 设置 Excel 导出的列名和列宽

## 环境要求

- **JDK**：21+
- **数据库**：MySQL 8.0+ 或 Oracle 12c+（用于存储元数据）
- **Redis**（可选）：用于缓存
- **Maven**：3.6+

## 下一步

- [安装部署](./installation) - 了解如何部署玉衡API
- [快速示例](./quick-example) - 通过一个完整示例体验核心功能
