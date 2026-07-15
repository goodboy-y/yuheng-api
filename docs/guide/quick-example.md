# 快速示例

本文将通过一个完整的示例，演示如何使用玉衡API将一条 SQL 查询转化为 API 接口。

## 场景

假设我们有一个 `person` 表，需要提供一个 API 接口来查询人员信息。

```sql
CREATE TABLE person (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    age INT,
    department VARCHAR(100)
);
```

## 步骤一：创建数据源

首先，创建一个指向目标数据库的数据源。

**请求：**

```http
POST /datasource/add
Content-Type: application/json

{
  "name": "业务数据库",
  "type": "mysql",
  "url": "jdbc:mysql://192.168.1.100:3306/business_db",
  "username": "readonly",
  "password": "password123"
}
```

::: tip
可以先使用 `/datasource/connect` 接口测试数据源连接是否可用。
:::

## 步骤二：创建客户端

创建一个 API 调用客户端，获得 `clientId` 和 `secret`。

客户端创建后，系统会自动生成 `clientId` 和 `secret`，请妥善保管。

## 步骤三：配置 API 接口

配置一个查询人员列表的 API：

**请求：**

```http
POST /apiConfig/add-with-mappings
Content-Type: application/json

{
  "apiConfig": {
    "name": "查询人员列表",
    "path": "/api/person",
    "datasource": { "id": "<数据源ID>" },
    "sql_param": "{\"sql\":\"SELECT id, name, age, department FROM person WHERE department = #{dept}\",\"params\":[{\"name\":\"dept\",\"type\":\"string\"}]}",
    "status": 1
  },
  "fieldMappings": [
    { "fieldName": "id", "displayName": "ID" },
    { "fieldName": "name", "displayName": "姓名" },
    { "fieldName": "age", "displayName": "年龄" },
    { "fieldName": "department", "displayName": "部门" }
  ],
  "pluginIds": []
}
```

::: info SQL 参数说明
- `sql`：要执行的 SQL 语句，使用 `#{paramName}` 作为参数占位符
- `params`：参数定义数组，每个参数包含 `name`（参数名）和 `type`（参数类型：`string` / `int` / `double`）
- 占位符会被自动替换为 `?`，使用 PreparedStatement 防止 SQL 注入
:::

## 步骤四：授权客户端访问

在管理界面中，将刚才创建的 API 授权给客户端。

## 步骤五：调用 API

### 使用 curl

```shell
curl -X GET 'http://localhost:8520/yuheng-api/api/person?dept=技术部' \
  -H 'clientId: <your-client-id>' \
  -H 'secret: <your-secret>'
```

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "age": 28,
      "department": "技术部"
    },
    {
      "id": 2,
      "name": "李四",
      "age": 32,
      "department": "技术部"
    }
  ],
  "timestamp": 1700000000000
}
```

### 使用 Java SDK

```java
YuhengClient client = new YuhengClient(
    "http://localhost:8520/yuheng-api",
    "<clientId>",
    "<secret>"
);

// 查询列表
Result<List<Map<String, Object>>> result = client.queryData("/api/person");
System.out.println(result.getData());

// 带参数查询
Map<String, Object> params = new HashMap<>();
params.put("dept", "技术部");
Result<List<Map<String, Object>>> result2 = client.queryData("/api/person", params);
```

## 添加分页功能

如果要为同一个 SQL 添加分页，只需在创建 API 时附加分页插件：

```json
{
  "apiConfig": { /* ... */ },
  "fieldMappings": [ /* ... */ ],
  "pluginIds": ["<分页插件ID>"]
}
```

调用时传入 `page` 和 `pageSize` 参数：

```shell
curl 'http://localhost:8520/yuheng-api/api/person?dept=技术部&page=1&pageSize=10' \
  -H 'clientId: <your-client-id>' \
  -H 'secret: <your-secret>'
```

分页响应中会包含 `pageInfo` 字段：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [ /* ... */ ],
  "pageInfo": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 42
  },
  "timestamp": 1700000000000
}
```

## 总结

通过以上步骤，我们完成了：

1. ✅ 创建数据源 - 连接业务数据库
2. ✅ 创建客户端 - 生成 API 调用凭证
3. ✅ 配置 API - 将 SQL 转换为 API 接口
4. ✅ 授权访问 - 控制 API 访问权限
5. ✅ 调用 API - 通过 HTTP 请求获取数据

整个过程无需编写任何 Java 代码，仅通过配置即可完成。
