# 管理端 API

管理端 API 用于管理数据源、API 配置、客户端、插件等，需要 JWT 认证。

## 认证

### 登录

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应：**
```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 刷新 Token

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 修改密码

```http
POST /auth/changePassword
Content-Type: application/json
Authorization: Bearer <accessToken>

{
  "oldPassword": "old-password",
  "newPassword": "new-password"
}
```

---

## 数据源管理

| 方法 | 路径                    | 说明             | 认证 |
| ---- | ----------------------- | ---------------- | ---- |
| GET  | `/datasource/search`    | 分页查询数据源    | JWT  |
| GET  | `/datasource/list`      | 获取所有数据源    | JWT  |
| POST | `/datasource/add`       | 创建数据源        | JWT  |
| GET  | `/datasource/detail/{id}` | 查看数据源详情  | JWT  |
| POST | `/datasource/update`    | 更新数据源        | JWT  |
| GET  | `/datasource/delete/{id}` | 删除数据源      | JWT  |
| POST | `/datasource/connect`   | 测试数据源连接    | JWT  |

---

## API 配置管理

| 方法 | 路径                              | 说明                   | 认证 |
| ---- | --------------------------------- | ---------------------- | ---- |
| GET  | `/apiConfig/search`               | 分页查询 API 配置       | JWT  |
| POST | `/apiConfig/add-with-mappings`    | 创建 API（含映射和插件） | JWT  |
| POST | `/apiConfig/update-with-mappings` | 更新 API（含映射和插件） | JWT  |
| GET  | `/apiConfig/detail/{id}`          | 查看 API 详情          | JWT  |
| GET  | `/apiConfig/detail-full/{id}`     | 查看 API 完整详情      | JWT  |
| GET  | `/apiConfig/delete/{id}`          | 删除 API              | JWT  |
| GET  | `/apiConfig/online/{id}`          | 上线 API              | JWT  |
| GET  | `/apiConfig/offline/{id}`         | 下线 API              | JWT  |
| GET  | `/apiConfig/parse-param`          | 解析 SQL 参数          | JWT  |
| GET  | `/apiConfig/parse-sql-fields`     | 解析 SQL 返回字段       | JWT  |
| GET  | `/apiConfig/unauthorized`         | 查询客户端未授权 API    | JWT  |
| POST | `/apiConfig/test/{apiId}`         | 测试 API               | JWT  |
| POST | `/apiConfig/export/{apiId}`       | 导出 API 测试数据       | JWT  |
| GET  | `/apiConfig/get-base-url`         | 获取服务基础 URL        | JWT  |
| GET  | `/apiConfig/remote-request`       | 代理远程请求            | JWT  |

---

## 客户端管理

| 方法 | 路径                      | 说明           | 认证 |
| ---- | ------------------------- | -------------- | ---- |
| GET  | `/apiClient/search`       | 分页查询客户端  | JWT  |
| POST | `/apiClient/add`          | 创建客户端      | JWT  |
| GET  | `/apiClient/detail/{id}`  | 查看客户端详情  | JWT  |
| POST | `/apiClient/update`       | 更新客户端      | JWT  |
| GET  | `/apiClient/delete/{id}`  | 删除客户端      | JWT  |

---

## 插件管理

| 方法 | 路径                      | 说明         | 认证 |
| ---- | ------------------------- | ------------ | ---- |
| GET  | `/apiPlugin/search`       | 查询插件列表  | JWT  |
| POST | `/apiPlugin/add`          | 创建插件      | JWT  |
| GET  | `/apiPlugin/detail/{id}`  | 查看插件详情  | JWT  |
| GET  | `/apiPlugin/delete/{id}`  | 删除插件      | JWT  |

---

## API 访问授权管理

| 方法 | 路径                           | 说明                | 认证 |
| ---- | ------------------------------ | ------------------- | ---- |
| GET  | `/apiConfigAccess/search`      | 查询授权记录         | JWT  |
| POST | `/apiConfigAccess/add`         | 授予访问权限         | JWT  |
| GET  | `/apiConfigAccess/delete/{id}` | 撤销访问权限         | JWT  |

---

## 访问日志

| 方法 | 路径                       | 说明         | 认证 |
| ---- | -------------------------- | ------------ | ---- |
| GET  | `/apiAccessLog/search`     | 查询访问日志  | JWT  |

---

## 统一响应格式

所有接口返回统一的 `Result<T>` 格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "pageInfo": {
    "pageNum": 0,
    "pageSize": 20,
    "total": 100
  },
  "timestamp": 1700000000000
}
```

### 返回码

| 状态码 | 说明     |
| ------ | -------- |
| 200    | 操作成功 |
| 201    | 限流/熔断 |
| 400    | 参数错误 |
| 401    | 认证失败 |
| 403    | 权限不足 |
| 9999   | 系统错误 |
