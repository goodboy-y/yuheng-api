# 数据源管理

数据源定义了玉衡API需要连接的目标数据库。系统支持同时管理多个数据源，每个 API 配置可以绑定到不同的数据源。

## 支持的数据库类型

| 数据库   | 驱动类                    | 连接池     |
| -------- | ------------------------- | ---------- |
| MySQL    | `com.mysql.cj.jdbc.Driver` | Druid      |
| Oracle   | `oracle.jdbc.OracleDriver` | Druid      |

## 数据源实体

```java
@Entity
@Table(name = "api_datasource")
public class ApiDatasource {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;        // UUID 主键
    private String name;      // 数据源名称（唯一）
    private String note;      // 备注说明
    private String url;       // JDBC 连接地址
    private String username;  // 数据库用户名
    private String password;  // 数据库密码
    private String type;      // 数据库类型：mysql / oracle
    private String accountId; // 所属账号
}
```

## API 接口

### 查询数据源列表

```http
GET /datasource/search?pageNum=0&pageSize=20&name=<关键字>
```

### 获取所有数据源

```http
GET /datasource/list
```

### 创建数据源

```http
POST /datasource/add
Content-Type: application/json

{
  "name": "生产数据库",
  "type": "mysql",
  "url": "jdbc:mysql://192.168.1.100:3306/prod_db",
  "username": "readonly",
  "password": "password123",
  "note": "生产环境只读账户"
}
```

### 测试数据源连接

创建数据源前，建议先测试连接：

```http
POST /datasource/connect
Content-Type: application/json

{
  "name": "测试连接",
  "type": "mysql",
  "url": "jdbc:mysql://192.168.1.100:3306/prod_db",
  "username": "readonly",
  "password": "password123"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "数据源连接成功",
  "data": null,
  "timestamp": 1700000000000
}
```

### 查看数据源详情

```http
GET /datasource/detail/{id}
```

### 更新数据源

```http
POST /datasource/update
Content-Type: application/json

{
  "id": "<数据源ID>",
  "name": "生产数据库（更新）",
  "url": "jdbc:mysql://192.168.1.101:3306/prod_db",
  "username": "readonly",
  "password": "new-password"
}
```

### 删除数据源

```http
GET /datasource/delete/{id}
```

::: warning 注意
删除数据源前，请确保没有任何 API 配置正在使用该数据源，否则 API 将无法正常执行。
:::

## 连接池管理

系统使用 Druid 连接池管理数据库连接，通过 `PoolManager` 为每个数据源维护独立的连接池：

```java
// 获取数据源对应的连接池连接
DruidPooledConnection connection = PoolManager.getPooledConnection(datasource);
```

连接池特性：

- 按数据源隔离，每个数据源独立连接池
- 自动创建和回收连接
- 支持连接池监控
