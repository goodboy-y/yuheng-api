# 数据库设计

玉衡API 使用关系型数据库（MySQL/Oracle）存储元数据，包括 API 配置、数据源、客户端、插件等。

## ER 图

```
┌─────────────────┐       ┌─────────────────┐
│   ApiAccount    │       │  ApiDatasource   │
├─────────────────┤       ├─────────────────┤
│ id       (PK)   │       │ id       (PK)   │
│ username        │       │ name     (UQ)   │
│ password        │       │ type            │
└────────┬────────┘       │ url             │
         │                │ username        │
         │ owns           │ password        │
         ▼                │ accountId (FK)  │
┌─────────────────┐       └────────┬────────┘
│   ApiClient     │                │
├─────────────────┤                │ references
│ id       (PK)   │                ▼
│ clientId (UQ)   │       ┌─────────────────┐
│ name            │       │   ApiConfig     │
│ secret          │       ├─────────────────┤
│ accountId (FK)  │       │ id       (PK)   │
└────────┬────────┘       │ name            │
         │                │ path     (UQ)    │
         │ authorizes     │ sql_param       │
         ▼                │ status          │
┌─────────────────┐       │ datasource_id(FK)│
│ ApiConfigAccess │       │ accountId (FK)  │
├─────────────────┤       └──┬──────┬───────┘
│ id       (PK)   │          │      │
│ apiConfigId(FK) │◄─────────┘      │
│ clientId  (FK)  │                 │
└─────────────────┘                 │
                                    │ has
                    ┌───────────────┘
                    ▼
┌─────────────────┐       ┌─────────────────┐
│ ApiFieldMapping │       │ ApiConfigPlugin │
├─────────────────┤       ├─────────────────┤
│ id       (PK)   │       │ id       (PK)   │
│ apiConfigId(FK) │       │ apiConfigId(FK) │
│ fieldName       │       │ pluginId  (FK)  │
│ displayName     │       └────────┬────────┘
│ columnWidth     │                │
└─────────────────┘                │ references
                                   ▼
                          ┌─────────────────┐
                          │   ApiPlugin     │
                          ├─────────────────┤
                          │ id       (PK)   │
                          │ name            │
                          │ className       │
                          │ accountId (FK)  │
                          └─────────────────┘

┌─────────────────┐       ┌──────────────────────┐
│ ApiAccessLog    │       │ ApiAccessLogArchive   │
├─────────────────┤       ├──────────────────────┤
│ id       (PK)   │──归档──►│ id            (PK)   │
│ apiPath         │       │ apiPath              │
│ clientId        │       │ clientId             │
│ params          │       │ params               │
│ accessTime      │       │ accessTime           │
│ accountId       │       │ accountId            │
└─────────────────┘       └──────────────────────┘
```

## 核心表结构

### api_datasource（数据源）

| 字段       | 类型         | 说明                     |
| ---------- | ------------ | ------------------------ |
| id         | VARCHAR(36)  | UUID 主键                 |
| name       | VARCHAR(255) | 数据源名称（唯一索引）      |
| note       | VARCHAR(255) | 备注                     |
| type       | VARCHAR(50)  | 数据库类型：mysql / oracle |
| url        | VARCHAR(500) | JDBC 连接地址             |
| username   | VARCHAR(100) | 数据库用户名              |
| password   | VARCHAR(255) | 数据库密码                |
| account_id | VARCHAR(36)  | 所属账号 ID               |

### api_config（API 配置）

| 字段           | 类型         | 说明                       |
| -------------- | ------------ | -------------------------- |
| id             | VARCHAR(36)  | UUID 主键                   |
| name           | VARCHAR(255) | API 名称                    |
| note           | VARCHAR(255) | 备注                       |
| path           | VARCHAR(255) | 访问路径（唯一索引）          |
| sql_param      | TEXT         | SQL 参数配置（JSON）         |
| status         | INT          | 状态：0-下线, 1-上线         |
| datasource_id  | VARCHAR(36)  | 关联数据源 ID（外键）         |
| account_id     | VARCHAR(36)  | 所属账号 ID                 |

### api_client（客户端）

| 字段       | 类型         | 说明             |
| ---------- | ------------ | ---------------- |
| id         | VARCHAR(36)  | UUID 主键         |
| client_id  | VARCHAR(255) | 客户端 ID（唯一）  |
| name       | VARCHAR(255) | 客户端名称        |
| secret     | VARCHAR(255) | 客户端密钥        |
| note       | VARCHAR(255) | 备注             |
| account_id | VARCHAR(36)  | 所属账号 ID       |

### api_plugin（插件）

| 字段       | 类型         | 说明                     |
| ---------- | ------------ | ------------------------ |
| id         | VARCHAR(36)  | UUID 主键                 |
| name       | VARCHAR(255) | 插件名称                  |
| class_name | VARCHAR(500) | 插件实现类全限定名          |
| note       | VARCHAR(255) | 备注                     |
| account_id | VARCHAR(36)  | 所属账号 ID               |

### api_config_plugin（API-插件关联）

| 字段           | 类型        | 说明         |
| -------------- | ----------- | ------------ |
| id             | VARCHAR(36) | UUID 主键     |
| api_config_id  | VARCHAR(36) | API 配置 ID   |
| plugin_id      | VARCHAR(36) | 插件 ID       |

### api_field_mapping（字段映射）

| 字段           | 类型         | 说明             |
| -------------- | ------------ | ---------------- |
| id             | VARCHAR(36)  | UUID 主键         |
| api_config_id  | VARCHAR(36)  | 关联 API 配置 ID  |
| field_name     | VARCHAR(255) | 数据库字段名       |
| display_name   | VARCHAR(255) | 展示名称          |
| column_width   | INT          | Excel 列宽        |

### api_config_access（API 访问授权）

| 字段           | 类型        | 说明             |
| -------------- | ----------- | ---------------- |
| id             | VARCHAR(36) | UUID 主键         |
| api_config_id  | VARCHAR(36) | API 配置 ID       |
| client_id      | VARCHAR(36) | 客户端 ID         |

### api_access_log（API 访问日志）

| 字段        | 类型         | 说明       |
| ----------- | ------------ | ---------- |
| id          | VARCHAR(36)  | UUID 主键   |
| api_path    | VARCHAR(255) | API 路径    |
| client_id   | VARCHAR(255) | 客户端 ID   |
| params      | TEXT         | 请求参数    |
| access_time | DATETIME     | 访问时间    |
| account_id  | VARCHAR(36)  | 所属账号    |

### api_account（账号）

| 字段     | 类型         | 说明     |
| -------- | ------------ | -------- |
| id       | VARCHAR(36)  | UUID 主键 |
| username | VARCHAR(100) | 用户名   |
| password | VARCHAR(255) | BCrypt 加密密码 |

## JPA 配置

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none  # 生产环境禁用自动 DDL
    show-sql: true    # 开发时显示 SQL
```

表结构需要手动创建或使用 Flyway/Liquibase 管理。
