# 贡献指南

感谢你对玉衡API的关注！以下是参与项目开发的指南。

## 开发环境搭建

### 前置要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+ 或 Oracle 12c+
- Redis
- IDE（推荐 IntelliJ IDEA）

### 克隆项目

```shell
git clone https://github.com/goodboy-y/yuheng-api
cd yuheng-api
```

### 配置数据库

创建元数据库并修改 `application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yuheng?characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 启动项目

```shell
./mvnw spring-boot:run
```

或使用 IDE 运行 `YuhengApiApplication`。

## 项目结构

```
src/main/java/com/compass/yuhengapi/
├── YuhengApiApplication.java    # 启动类
├── common/                       # 公共模块
│   ├── enumerate/                # 枚举（返回码等）
│   ├── handler/                  # 全局异常处理
│   ├── lang/                     # 基础类（异常、查询命令）
│   └── util/                     # 工具类（Result、分页、Excel等）
├── conf/                         # 配置类
│   ├── CrossConfig.java          # 跨域配置
│   ├── PluginInitListener.java   # 插件初始化监听器
│   └── SecurityConfig.java       # 安全配置
├── controller/                   # 控制器层
├── filter/                       # 过滤器
│   ├── ApiSecretFilter.java      # API 客户端鉴权
│   └── JwtFilter.java            # JWT 鉴权
├── model/                        # 数据模型
│   ├── bean/                     # 内部 Bean
│   ├── converter/                # MapStruct 转换器
│   ├── dto/                      # 数据传输对象
│   ├── entities/                 # JPA 实体
│   └── vo/                       # 视图对象
├── plugin/                       # 插件系统
│   ├── Plugin.java               # 插件接口
│   └── impl/                     # 插件实现
├── repo/                         # JPA Repository
├── service/                      # 服务层接口
│   └── impl/                     # 服务层实现
├── task/                         # 定时任务
└── util/                         # 工具类
    ├── DbType.java               # 数据库类型
    ├── JdbcUtil.java             # JDBC 工具
    ├── JwtUtil.java              # JWT 工具
    ├── PasswordUtil.java         # 密码工具
    └── PoolManager.java          # 连接池管理器
```

## 代码规范

### 命名规范

- 类名：大驼峰（`ApiConfigService`）
- 方法名：小驼峰（`executeSql`）
- 常量：全大写带下划线（`DEFAULT_PAGE_SIZE`）
- 包名：全小写

### 分层职责

| 层         | 职责                                     |
| ---------- | ---------------------------------------- |
| Controller | 接收请求、参数校验、调用 Service、返回结果 |
| Service    | 业务逻辑编排                             |
| Repository | 数据访问                                 |
| Plugin     | 可插拔的请求/响应/SQL 处理逻辑            |
| Filter     | 请求鉴权和日志                           |

### 使用 Lombok

```java
// 推荐
@RequiredArgsConstructor  // 构造器注入
@Slf4j                    // 日志
@Getter @Setter           // 实体类
```

## 开发插件

1. 实现 `Plugin` 接口
2. 在管理端创建插件记录（填写 `className`）
3. 在 API 配置中关联插件

```java
package com.compass.yuhengapi.plugin.impl;

import com.compass.yuhengapi.plugin.Plugin;
// ...

public class MyPlugin implements Plugin {
    @Override
    public String getName() {
        return "我的插件";
    }

    // 实现其他接口方法...
}
```

## 提交流程

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/my-feature`
3. 提交代码：`git commit -m 'feat: 添加某某功能'`
4. 推送分支：`git push origin feature/my-feature`
5. 创建 Pull Request

## 提交规范

使用约定式提交：

- `feat:` 新功能
- `fix:` 修复 Bug
- `docs:` 文档更新
- `refactor:` 代码重构
- `test:` 测试相关
- `chore:` 构建/工具相关

## 构建

```shell
# 编译
./mvnw clean compile

# 打包
./mvnw clean package -DskipTests

# 构建 Docker 镜像
./mvnw spring-boot:build-image
```

## 许可证

本项目基于 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 发布。
