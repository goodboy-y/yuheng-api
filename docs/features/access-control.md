# 访问控制

玉衡API 提供两层访问控制机制：客户端鉴权（`ApiSecretFilter`）和用户认证（JWT），确保 API 调用的安全性。

## 两层鉴权架构

```
┌──────────────────────────────────────┐
│           请求到达                     │
├──────────────────────────────────────┤
│  第一层：ApiSecretFilter              │
│  ├─ 仅对 /api/** 路径生效             │
│  ├─ 校验 clientId + secret            │
│  └─ 校验客户端对 API 的访问权限         │
├──────────────────────────────────────┤
│  第二层：JwtFilter                    │
│  ├─ 对管理端接口生效                    │
│  └─ JWT Token 认证                    │
├──────────────────────────────────────┤
│           Controller 处理              │
└──────────────────────────────────────┘
```

## 客户端鉴权（ApiSecretFilter）

### 认证流程

1. 检查请求路径是否以 `/api/` 开头（只对数据 API 生效）
2. 从请求头获取 `clientId` 和 `secret`
3. 查询 `clientId` 对应的客户端信息
4. 比对 `secret` 是否匹配
5. 校验客户端是否有权限访问该 API
6. 记录访问日志
7. 放行或拒绝请求

### 请求示例

```shell
curl 'http://localhost:8520/yuheng-api/api/person?dept=技术部' \
  -H 'clientId: w8hpmb7T1xbWmsGr' \
  -H 'secret: XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW'
```

### 认证失败响应

```json
{
  "code": 401,
  "message": "无效的访问令牌",
  "data": null,
  "timestamp": 1700000000000
}
```

### 权限不足响应

```json
{
  "code": 403,
  "message": "未授权访问此API",
  "data": null,
  "timestamp": 1700000000000
}
```

## 用户认证（JWT）

管理端接口（如数据源管理、API 配置等）使用 JWT 进行用户认证。

### 登录

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

成功响应：

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### Token 刷新

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

{
  "oldPassword": "old-password",
  "newPassword": "new-password"
}
```

### JWT 配置

```yaml
jwt:
  secret: "bGl1eXVpc2Fnb2Rib3k="  # 至少 32 字符
  expiration: 1800000              # accessToken 过期时间：30分钟
  refresh-expiration: 86400000     # refreshToken 过期时间：24小时
```

## API 访问授权

客户端需要被授权后才能访问特定的 API。

### 授权管理 API

查询客户端的未授权 API：
```http
GET /apiConfig/unauthorized?clientId=<客户端ID>&pageNum=0&pageSize=20
```

查询 API 的已授权客户端：
```http
GET /apiConfigAccess/search?apiConfigId=<API_ID>
```

授予访问权限：
```http
POST /apiConfigAccess/add
Content-Type: application/json

{
  "apiConfigId": "<API_ID>",
  "clientId": "<客户端ID>"
}
```

撤销访问权限：
```http
GET /apiConfigAccess/delete/{id}
```

## 安全配置

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(
                "/auth/login", "/auth/logout", "/auth/refresh",
                "/api/**", "/assets/**", "/"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(secretFilter(), ...)
        .addFilterBefore(jwtFilter(), ...);

    return http.build();
}
```

- 所有 `/api/**` 路径公开访问，由 `ApiSecretFilter` 处理鉴权
- 管理端接口需要 JWT 认证
- 无状态会话（STATELESS），适合微服务架构
- 禁用 CSRF（API 服务不需要）
