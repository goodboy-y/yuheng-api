# 架构概览

玉衡API 基于 Spring Boot 4.0 构建，采用分层架构设计。

## 整体架构

```
┌──────────────────────────────────────────────────────────┐
│                        前端 (Thymeleaf)                    │
├──────────────────────────────────────────────────────────┤
│                     Controller 层                          │
│  ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌───────────┐  │
│  │  Index   │ │   Auth    │ │   Api    │ │ ApiConfig │  │
│  │Controller│ │Controller │ │Controller│ │Controller │  │
│  └──────────┘ └───────────┘ └──────────┘ └───────────┘  │
│  ┌──────────┐ ┌───────────┐ ┌───────────┐ ┌──────────┐  │
│  │ApiClient │ │ApiDataSource│ │ApiPlugin │ │ApiAccess │  │
│  │Controller│ │ Controller │ │Controller│ │LogCtrl   │  │
│  └──────────┘ └───────────┘ └───────────┘ └──────────┘  │
├──────────────────────────────────────────────────────────┤
│                      Filter 层                             │
│  ┌──────────────┐  ┌──────────────┐                       │
│  │ JwtFilter    │  │ApiSecretFilter│                      │
│  │ (管理端JWT)  │  │ (API客户端鉴权)│                     │
│  └──────────────┘  └──────────────┘                       │
├──────────────────────────────────────────────────────────┤
│                     Service 层                             │
│  ┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐  │
│  │   Api    │ │ApiConfig │ │ApiClient  │ │ApiPlugin │  │
│  │ Service  │ │ Service  │ │ Service   │ │ Service  │  │
│  └──────────┘ └──────────┘ └───────────┘ └──────────┘  │
│  ┌──────────┐ ┌───────────┐ ┌───────────┐               │
│  │  Auth    │ │ ApiDataSource│ │ApiConfig │               │
│  │ Service  │ │  Service   │ │AccessSvc │               │
│  └──────────┘ └───────────┘ └───────────┘               │
├──────────────────────────────────────────────────────────┤
│                      Plugin 层                             │
│  ┌─────────────┐  ┌──────────────────┐                    │
│  │ Pagination  │  │  Custom Plugin   │ ...                │
│  │   Plugin    │  │                  │                    │
│  └─────────────┘  └──────────────────┘                    │
├──────────────────────────────────────────────────────────┤
│                   Repository 层 (JPA)                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │ApiConfig │ │ApiClient │ │ApiAccount│ │ApiPlugin  │  │
│  │  Repo    │ │  Repo    │ │  Repo    │ │   Repo    │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘  │
├──────────────────────────────────────────────────────────┤
│                    基础设施层                               │
│  ┌──────┐ ┌───────┐ ┌──────┐ ┌──────┐ ┌──────────────┐  │
│  │Druid │ │Redis  │ │Cache │ │Resil-│ │GraalVM       │  │
│  │连接池│ │       │ │Caffeine│ │ience4j│ │Native Image │  │
│  └──────┘ └───────┘ └──────┘ └──────┘ └──────────────┘  │
└──────────────────────────────────────────────────────────┘
```

## 核心模块

### 1. API 执行引擎（`ApiServiceImpl`）

API 执行引擎是整个系统的核心，负责将 HTTP 请求转换为 SQL 执行：

```
HTTP Request → buildSql() → Plugin.processRequest() → Plugin.processSql()
→ JdbcUtil.query() → Plugin.processResponse() → Result<Object>
```

关键步骤：
1. **加载插件**：根据 API 配置加载关联的插件实例
2. **处理请求参数**：按顺序调用每个插件的 `processRequest`
3. **构建 SQL**：将 `#{paramName}` 占位符替换为 `?`，参数类型转换
4. **SQL 处理**：按顺序调用每个插件的 `processSql`
5. **执行查询**：通过 Druid 连接池执行 SQL
6. **响应处理**：按顺序调用每个插件的 `processResponse`

### 2. 过滤器链

请求通过两层过滤器：

```
Request → ApiSecretFilter → JwtFilter → Controller
```

- `ApiSecretFilter`：仅处理 `/api/**` 路径，校验 clientId/secret 和 API 访问权限
- `JwtFilter`：处理管理端请求的 JWT 认证

### 3. 插件系统

插件通过 SPI 机制加载，支持在请求/响应/SQL 各阶段介入：

```java
public interface Plugin {
    String getName();
    String getDescription();
    void init(ApiConfig apiConfig);
    Map<String, Object> processRequest(HttpServletRequest request, Map<String, Object> params);
    Object processResponse(Object response);
    ApiSql processSql(ApiSql apiSql, Map<String, Object> params, String databaseType);
}
```

### 4. 连接池管理

`PoolManager` 为每个数据源维护独立的 Druid 连接池，按数据源 ID 隔离：

```java
public class PoolManager {
    // 数据源ID → DruidDataSource 的映射
    private static final Map<String, DruidDataSource> poolMap;
    
    public static DruidPooledConnection getPooledConnection(ApiDatasource datasource);
}
```

## 技术选型

| 组件          | 技术                | 说明                       |
| ------------- | ------------------- | -------------------------- |
| 框架          | Spring Boot 4.0     | 最新版本，Java 21 + 虚拟线程 |
| 安全          | Spring Security     | 无状态会话 + JWT            |
| ORM           | Spring Data JPA     | 元数据存储                  |
| 连接池        | Druid 1.1.20        | 支持多数据源独立连接池       |
| 缓存          | Caffeine + Redis    | 本地缓存 + 分布式缓存        |
| 限流熔断      | Resilience4j        | 限流器 + 熔断器             |
| SQL 解析      | JSqlParser 5.3      | SQL 语法解析                |
| JSON          | Fastjson2 2.0.40    | 高性能 JSON 处理            |
| Excel         | Apache POI 5.2.5    | Excel 文件生成              |
| 工具库        | Hutool 5.8.40       | 通用工具                    |
| Bean 映射     | MapStruct 1.6.3     | 编译期代码生成              |
| 模板引擎      | Thymeleaf           | 管理端页面渲染              |
| 原生编译      | GraalVM Native Image| 毫秒级启动                  |

## 高可用保障

### 限流（Rate Limiter）

```yaml
resilience4j:
  ratelimiter:
    instances:
      apiRateLimiter:
        limit-for-period: 2000    # 每秒 2000 个请求
        limit-refresh-period: 1s
        timeout-duration: 2000ms  # 获取令牌超时时间
```

### 熔断（Circuit Breaker）

```yaml
resilience4j:
  circuitbreaker:
    instances:
      apiCircuitBreaker:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 100           # 100次调用窗口
        failure-rate-threshold: 50         # 50%失败率触发熔断
        wait-duration-in-open-state: 5s    # 熔断后等待5秒
        permitted-number-of-calls-in-half-open-state: 10
```

## 定时任务

系统包含一个定时任务用于访问日志归档：

```java
@Component
public class ApiAccessLogArchiveTask {
    // 定期将 ApiAccessLog 归档到 ApiAccessLogArchive 表
}
```
