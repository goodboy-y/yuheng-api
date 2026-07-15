# API 配置

API 配置是玉衡API的核心功能，用于将一条 SQL 查询语句转换为 RESTful API 接口。

## API 配置实体

```java
@Entity
@Table(name = "api_config")
public class ApiConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;            // UUID 主键
    private String name;          // API 名称
    private String note;          // 备注
    private String path;          // 访问路径（唯一）
    private String sql_param;     // SQL 参数配置（JSON 格式）
    private Integer status;       // 状态：0-下线, 1-上线
    private String accountId;     // 所属账号

    @ManyToOne
    private ApiDatasource datasource;  // 关联的数据源

    @OneToMany(mappedBy = "apiConfig")
    private List<ApiConfigPlugin> plugins;  // 关联的插件
}
```

## SQL 参数格式

`sql_param` 字段是一个 JSON 字符串，包含 SQL 模板和参数定义：

```json
{
  "sql": "SELECT * FROM person WHERE department = #{dept} AND age > #{minAge}",
  "params": [
    { "name": "dept", "type": "string" },
    { "name": "minAge", "type": "int" }
  ]
}
```

### 参数类型

| 类型     | 说明       | Java 映射    |
| -------- | ---------- | ------------ |
| `string` | 字符串类型 | `String`     |
| `int`    | 整数类型   | `Long`       |
| `double` | 浮点类型   | `Double`     |

### SQL 占位符

在 SQL 中使用 `#{paramName}` 作为占位符，系统会自动：

1. 将 `#{paramName}` 替换为 JDBC 的 `?` 占位符
2. 根据参数类型进行值转换
3. 使用 `PreparedStatement` 执行，**防止 SQL 注入**

## API 接口

### 查询 API 列表

```http
GET /apiConfig/search?pageNum=0&pageSize=20&name=<关键字>
```

### 创建 API（含字段映射和插件）

```http
POST /apiConfig/add-with-mappings
Content-Type: application/json

{
  "apiConfig": {
    "name": "查询人员",
    "path": "/api/person",
    "datasource": { "id": "<数据源ID>" },
    "sql_param": "{\"sql\":\"SELECT * FROM person WHERE dept = #{dept}\",\"params\":[{\"name\":\"dept\",\"type\":\"string\"}]}",
    "status": 0,
    "note": "按部门查询人员信息"
  },
  "fieldMappings": [
    { "fieldName": "id", "displayName": "编号", "columnWidth": 80 },
    { "fieldName": "name", "displayName": "姓名", "columnWidth": 120 },
    { "fieldName": "age", "displayName": "年龄", "columnWidth": 60 },
    { "fieldName": "dept", "displayName": "部门", "columnWidth": 150 }
  ],
  "pluginIds": ["<分页插件ID>"]
}
```

### 更新 API

```http
POST /apiConfig/update-with-mappings
Content-Type: application/json

{
  "apiConfig": { /* 包含 id 字段的完整配置 */ },
  "fieldMappings": [ /* 更新后的字段映射 */ ],
  "pluginIds": [ /* 更新后的插件列表 */ ]
}
```

### 获取 API 详情

```http
GET /apiConfig/detail/{id}
```

### 获取 API 完整详情（含映射和插件）

```http
GET /apiConfig/detail-full/{id}
```

响应包含 API 配置、字段映射列表和插件配置。

### API 上/下线

```http
# 上线
GET /apiConfig/online/{id}

# 下线
GET /apiConfig/offline/{id}
```

下线的 API 将无法通过 `/api/{path}` 访问。

### 删除 API

```http
GET /apiConfig/delete/{id}
```

### 解析 SQL 参数

在创建 API 前，可以预览 SQL 中的参数：

```http
GET /apiConfig/parse-param?sql=SELECT * FROM person WHERE dept = #{dept}
```

响应：

```json
{
  "code": 200,
  "data": [
    { "name": "dept", "type": "string" }
  ]
}
```

### 解析 SQL 字段

预览 SQL 查询将返回的字段列表：

```http
GET /apiConfig/parse-sql-fields?datasourceId=<数据源ID>&sql=SELECT id, name FROM person
```

### 获取未授权 API 列表

查询某个客户端尚未获得授权的 API：

```http
GET /apiConfig/unauthorized?clientId=<客户端ID>&pageNum=0&pageSize=20
```

### 测试 API

在管理端测试 API 执行：

```http
POST /apiConfig/test/{apiId}
Content-Type: application/json

{
  "dept": "技术部"
}
```

## API 调用模式

配置完成后，API 通过以下路径对外提供服务：

```
GET /api/{path}?param1=value1&param2=value2
```

请求头需要携带鉴权信息：

```
clientId: <客户端ID>
secret: <客户端密钥>
```
