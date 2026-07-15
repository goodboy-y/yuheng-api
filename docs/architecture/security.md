# 安全设计

玉衡API 采用多层安全防护体系，涵盖认证、鉴权、SQL 注入防护、限流熔断等方面。

## 安全架构概览

```
┌─────────────────────────────────────────┐
│              安全层                      │
├─────────────────────────────────────────┤
│  传输安全    HTTPS（生产环境推荐）         │
├─────────────────────────────────────────┤
│  认证层      JWT（管理端）+ ClientId/Secret（API端）│
├─────────────────────────────────────────┤
│  鉴权层      API 访问授权（ApiConfigAccess）│
├─────────────────────────────────────────┤
│  SQL 防护    PreparedStatement 参数化查询   │
├─────────────────────────────────────────┤
│  限流熔断    Resilience4j RateLimiter + CircuitBreaker │
├─────────────────────────────────────────┤
│  密码存储    BCrypt 加密                  │
└─────────────────────────────────────────┘
```

## 1. 认证机制

### 管理端认证（JWT）

```
┌─────────┐     ┌──────────┐     ┌─────────────┐
│  浏览器  │────►│ /auth/login │────►│ Authentication │
│         │     │            │     │   Manager    │
└─────────┘     └──────────┘     └──────┬──────┘
                                        │
                               ┌────────▼────────┐
                               │ UserDetailsService│
                               │ (ApiAccountRepo) │
                               └────────┬────────┘
                                        │
                               ┌────────▼────────┐
                               │   生成 JWT Token │
                               │  accessToken    │
                               │  refreshToken   │
                               └─────────────────┘
```

**JWT 配置：**
```yaml
jwt:
  secret: "base64-encoded-secret"  # HMAC-SHA256 签名密钥
  expiration: 1800000              # 30 分钟
  refresh-expiration: 86400000     # 24 小时
```

**JWT 过滤器（JwtFilter）：**
```java
public class JwtFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String token = extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 设置 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities())
            );
        }
        filterChain.doFilter(request, response);
    }
}
```

### API 端认证（ClientId/Secret）

API 调用方通过请求头传入凭证：

```
clientId: <客户端ID>
secret: <客户端密钥>
```

由 `ApiSecretFilter` 校验。

## 2. SQL 注入防护

系统通过以下机制防止 SQL 注入：

### PreparedStatement 参数化查询

SQL 中的 `#{paramName}` 占位符被替换为 `?`，由 JDBC PreparedStatement 处理：

```java
// 用户 SQL 模板
String sql = "SELECT * FROM person WHERE name = #{name}";

// 系统处理
// 1. #{name} → ?
// 2. 参数类型转换
// 3. PreparedStatement 执行
Object[] params = new Object[]{ "张三" };
ResultSet rs = JdbcUtil.query(sql, connection, params);
// 实际执行: SELECT * FROM person WHERE name = ?
```

::: warning 安全限制
SQL 模板中只允许 `SELECT` 查询，不支持 `INSERT`、`UPDATE`、`DELETE`、`DROP` 等写操作。
:::

## 3. 密码安全

### 存储加密

用户密码使用 BCrypt 加密存储：

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 修改密码

```java
public void changePassword(String username, ChangePasswordDto dto) {
    ApiAccount account = apiAccountRepository.findByUsername(username);
    // 验证旧密码
    if (!passwordEncoder.matches(dto.getOldPassword(), account.getPassword())) {
        throw new APIException("旧密码不正确");
    }
    // 加密新密码
    account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    apiAccountRepository.save(account);
}
```

## 4. 限流与熔断

### 限流器

保护 API 不被过度调用：

```java
@RateLimiter(name = "apiRateLimiter", fallbackMethod = "rateLimitFallback")
public Result<Object> api(String path, HttpServletRequest request) {
    // ...
}

public Result<Object> rateLimitFallback(String path, HttpServletRequest request, Throwable t) {
    return Result.fail("请求过于频繁，请稍后再试")
        .setCode(ReturnCodeEnum.RC201.getCode());
}
```

### 熔断器

当错误率超过阈值时自动熔断：

```java
@CircuitBreaker(name = "apiCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
public Result<Object> api(String path, HttpServletRequest request) {
    // ...
}

public Result<Object> circuitBreakerFallback(String path, HttpServletRequest request, Throwable t) {
    return Result.fail("服务暂时不可用，请稍后再试")
        .setCode(ReturnCodeEnum.RC201.getCode());
}
```

## 5. CORS 配置

```java
@Configuration
public class CrossConfig {
    // 配置跨域访问，允许前端调用
}
```

## 6. 安全最佳实践

### 生产环境检查清单

- [ ] 修改默认 JWT secret 为强随机密钥（至少 256 位）
- [ ] 启用 HTTPS，禁止 HTTP 明文传输
- [ ] 修改默认数据库密码
- [ ] 配置 Redis 密码
- [ ] 根据业务需求调整限流阈值
- [ ] 定期轮换客户端 secret
- [ ] 审计 API 访问日志
- [ ] 限制管理端访问 IP（通过防火墙或反向代理）

### 密钥管理

```shell
# 生成安全的 JWT secret（Linux/Mac）
openssl rand -base64 64

# 或使用 Java
java -cp ... GenerateSecret
```
