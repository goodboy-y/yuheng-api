# Java SDK

玉衡API 提供 Java 客户端 SDK（`yuheng-sdk`），方便 Java 项目快速集成。

## 安装

### Maven

```xml
<dependency>
    <groupId>io.github.goodboy-y</groupId>
    <artifactId>yuheng-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.goodboy-y:yuheng-sdk:0.0.1'
```

## 快速开始

### 1. 创建客户端

```java
import io.github.yuhengapi.sdk.YuhengClient;

public class Example {
    public static void main(String[] args) {
        YuhengClient client = new YuhengClient(
            "http://localhost:8520/yuheng-api",  // 玉衡API 服务地址
            "w8hpmb7T1xbWmsGr",                   // clientId
            "XQUIBw4fi5umtb3ckEHi4h3YXLjqXhtX5g9XDUB4EqYszov3A6cTHXZHasDn46CW"  // secret
        );

        // 开始调用 API
    }
}
```

### 2. 查询数据

```java
// 无参数查询
Result<List<Map<String, Object>>> result = client.queryData("/api/person");
System.out.println(result.getData());

// 带参数查询
Map<String, Object> params = new HashMap<>();
params.put("dept", "技术部");
params.put("minAge", 25);
Result<List<Map<String, Object>>> result2 = client.queryData("/api/person", params);
```

### 3. 分页查询

```java
// 分页查询
Result<List<Map<String, Object>>> pageResult = client.queryPage(
    "/api/person",
    null,     // 查询参数（null 表示无参数）
    0,        // 页码（从 0 开始）
    10        // 每页大小
);

// 获取分页信息
PageInfo pageInfo = pageResult.getPageInfo();
System.out.println("当前页：" + pageInfo.getPageNum());
System.out.println("每页大小：" + pageInfo.getPageSize());
System.out.println("总记录数：" + pageInfo.getTotal());
```

### 4. 带参数分页查询

```java
Map<String, Object> params = new HashMap<>();
params.put("dept", "技术部");

Result<List<Map<String, Object>>> pageResult = client.queryPage(
    "/api/person",
    params,
    0,
    20
);
```

## API 参考

### YuhengClient

| 方法                                                                    | 说明             |
| ----------------------------------------------------------------------- | ---------------- |
| `YuhengClient(String baseUrl, String clientId, String secret)`          | 构造函数         |
| `queryData(String path)`                                                | 无参数查询       |
| `queryData(String path, Map<String, Object> params)`                   | 带参数查询       |
| `queryPage(String path, Map<String, Object> params, int page, int size)` | 分页查询         |

### `Result<T>`

| 方法            | 说明         |
| --------------- | ------------ |
| `getCode()`     | 获取状态码    |
| `getMessage()`  | 获取提示信息  |
| `getData()`     | 获取数据      |
| `getPageInfo()` | 获取分页信息  |

### PageInfo

| 方法           | 说明       |
| -------------- | ---------- |
| `getPageNum()`  | 当前页码   |
| `getPageSize()` | 每页大小   |
| `getTotal()`    | 总记录数   |

## 完整示例

```java
import io.github.yuhengapi.sdk.YuhengClient;
import io.github.yuhengapi.sdk.Result;
import io.github.yuhengapi.sdk.PageInfo;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class YuhengDemo {

    public static void main(String[] args) {
        // 1. 创建客户端
        YuhengClient client = new YuhengClient(
            "http://localhost:8520/yuheng-api",
            "your-client-id",
            "your-secret"
        );

        // 2. 无参数查询
        Result<List<Map<String, Object>>> result1 = client.queryData("/api/departments");
        if (result1.getCode() == 200) {
            List<Map<String, Object>> departments = result1.getData();
            for (Map<String, Object> dept : departments) {
                System.out.println(dept.get("deptName"));
            }
        } else {
            System.err.println("查询失败: " + result1.getMessage());
        }

        // 3. 带参数查询
        Map<String, Object> params = new HashMap<>();
        params.put("dept", "技术部");
        Result<List<Map<String, Object>>> result2 = client.queryData("/api/person", params);
        if (result2.getCode() == 200) {
            for (Map<String, Object> person : result2.getData()) {
                System.out.printf("%s - %s - %d岁%n",
                    person.get("name"),
                    person.get("department"),
                    person.get("age"));
            }
        }

        // 4. 分页查询
        Result<List<Map<String, Object>>> result3 = client.queryPage(
            "/api/person", params, 0, 10
        );
        if (result3.getCode() == 200) {
            PageInfo pageInfo = result3.getPageInfo();
            System.out.printf("共 %d 条记录，当前第 %d 页%n",
                pageInfo.getTotal(),
                pageInfo.getPageNum() + 1);  // SDK 页码从 0 开始
        }
    }
}
```

## 错误处理

SDK 不会抛出异常，所有错误信息通过 `Result` 对象返回：

```java
Result<List<Map<String, Object>>> result = client.queryData("/api/nonexistent");

if (result.getCode() != 200) {
    System.err.println("错误码: " + result.getCode());
    System.err.println("错误信息: " + result.getMessage());
    // 错误码 9999: 系统错误（如接口不存在）
    // 错误码 201:  限流/熔断
    // 错误码 401:  认证失败
    // 错误码 403:  权限不足
}
```

## Spring Boot 集成

在 Spring Boot 项目中，可以将 `YuhengClient` 注册为 Bean：

```java
@Configuration
public class YuhengConfig {

    @Value("${yuheng.api.base-url}")
    private String baseUrl;

    @Value("${yuheng.api.client-id}")
    private String clientId;

    @Value("${yuheng.api.secret}")
    private String secret;

    @Bean
    public YuhengClient yuhengClient() {
        return new YuhengClient(baseUrl, clientId, secret);
    }
}
```

配置 `application.yml`：

```yaml
yuheng:
  api:
    base-url: http://localhost:8520/yuheng-api
    client-id: your-client-id
    secret: your-secret
```

使用：

```java
@Service
@RequiredArgsConstructor
public class PersonService {

    private final YuhengClient yuhengClient;

    public List<Map<String, Object>> getPersonsByDept(String dept) {
        Map<String, Object> params = Map.of("dept", dept);
        Result<List<Map<String, Object>>> result = yuhengClient.queryData("/api/person", params);
        if (result.getCode() == 200) {
            return result.getData();
        }
        throw new RuntimeException("查询失败: " + result.getMessage());
    }
}
```
