# 数据 API

数据 API 是玉衡API对外提供的核心服务，通过配置好的路径向外部系统提供数据查询服务。

## API 端点

### 查询数据

```http
GET /api/{path}?param1=value1&param2=value2
```

### 导出数据

```http
GET /api/export/{path}?param1=value1&param2=value2
```

## 请求头

| 请求头    | 必填 | 说明           |
| --------- | ---- | -------------- |
| clientId  | 是   | 客户端 ID       |
| secret    | 是   | 客户端密钥      |

## 认证流程

```
1. 客户端在请求头中携带 clientId 和 secret
2. ApiSecretFilter 校验凭证有效性
3. ApiSecretFilter 校验客户端对该 API 的访问权限
4. 通过后执行 SQL 并返回数据
```

## 请求示例

### 基本查询

```shell
curl -X GET 'http://localhost:8520/yuheng-api/api/person?dept=技术部' \
  -H 'clientId: w8hpmb7T1xbWmsGr' \
  -H 'secret: XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW'
```

### 分页查询（需配置分页插件）

```shell
curl -X GET 'http://localhost:8520/yuheng-api/api/person?dept=技术部&page=1&pageSize=20' \
  -H 'clientId: w8hpmb7T1xbWmsGr' \
  -H 'secret: XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW'
```

### 导出 Excel

```shell
curl -o person.xlsx \
  'http://localhost:8520/yuheng-api/api/export/person?dept=技术部' \
  -H 'clientId: w8hpmb7T1xbWmsGr' \
  -H 'secret: XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW'
```

## 响应格式

### 普通查询响应

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

### 分页查询响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "name": "张三" },
    { "id": 2, "name": "李四" }
  ],
  "pageInfo": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 150
  },
  "timestamp": 1700000000000
}
```

### 错误响应

```json
{
  "code": 9999,
  "message": "该接口不存在！！",
  "data": null,
  "timestamp": 1700000000000
}
```

### 限流响应

```json
{
  "code": 201,
  "message": "请求过于频繁，请稍后再试",
  "data": null,
  "timestamp": 1700000000000
}
```

### 熔断响应

```json
{
  "code": 201,
  "message": "服务暂时不可用，请稍后再试",
  "data": null,
  "timestamp": 1700000000000
}
```

## 响应字段说明

| 字段        | 类型      | 说明                         |
| ----------- | --------- | ---------------------------- |
| code        | Integer   | 状态码：200-成功, 201-限流, 9999-错误 |
| message     | String    | 提示信息                      |
| data        | Array     | 查询结果数组，每条记录为 JSON 对象 |
| pageInfo    | Object    | 分页信息（仅分页查询时返回）    |
| pageInfo.pageNum  | Integer | 当前页码（从 1 开始）      |
| pageInfo.pageSize | Integer | 每页记录数                |
| pageInfo.total    | Integer | 总记录数                  |
| timestamp   | Long      | 响应时间戳（毫秒）             |

## 字段命名规则

数据库列名会自动进行驼峰转换：

| 数据库列名         | API 返回字段     |
| ------------------ | ---------------- |
| `user_name`        | `userName`       |
| `department_id`    | `departmentId`   |
| `created_at`       | `createdAt`      |

## 参数说明

### 请求参数

API 的请求参数由 SQL 模板中的 `#{paramName}` 占位符定义，通过 URL 查询参数传入：

```
GET /api/person?dept=技术部&minAge=25
```

对应的 SQL 模板：

```sql
SELECT * FROM person WHERE department = #{dept} AND age > #{minAge}
```

### 分页参数（需要分页插件）

| 参数     | 类型    | 默认值 | 说明         |
| -------- | ------- | ------ | ------------ |
| page     | Integer | 1      | 页码（从1开始）|
| pageSize | Integer | 20     | 每页记录数（最大100）|

## 限流说明

- 默认限制：每秒 2000 个请求
- 超过限制后返回状态码 201
- 可通过配置文件调整阈值

## 熔断说明

- 滑动窗口大小：100 次调用
- 失败率阈值：50%
- 熔断后等待时间：5 秒
- 半开状态允许调用数：10
