# 安装部署

## 数据库初始化

部署前需先执行数据库脚本，脚本位于项目 `src/main/resources/db/` 目录下。

### MySQL

```shell
# 1. 创建数据库及表结构
mysql -h <host> -u <username> -p < src/main/resources/db/mysql_create_tables.sql

# 2. 导入初始数据
mysql -h <host> -u <username> -p <database> < src/main/resources/db/data.sql
```

### Oracle

```shell
# 1. 创建表结构
sqlplus <username>/<password>@<host>:<port>/<service> @src/main/resources/db/oracle_create_tables.sql

# 2. 导入初始数据
sqlplus <username>/<password>@<host>:<port>/<service> @src/main/resources/db/data.sql
```

> **说明：** `mysql_create_tables.sql` / `oracle_create_tables.sql` 负责建表，`data.sql` 负责插入初始数据（默认插件、管理员账号等）。

## Docker 部署（推荐）

```shell
docker run -d --name=yuheng-api \
  -p 8520:8520 \
  -e SERVER_PORT=8520 \
  -e CONTEXT_PATH=/yuheng-api \
  -e SPRING_REDIS_HOST=your-redis-host \
  -e SPRING_REDIS_PASSWORD='your-redis-password' \
  -e SPRING_DATASOURCE_URL='jdbc:mysql://titi.vip:3306/yuheng?characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true' \
  -e SPRING_DATASOURCE_USERNAME=your-db-username \
  -e SPRING_DATASOURCE_PASSWORD=your-db-password \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver \
  registry.cn-hangzhou.aliyuncs.com/tianxuan/yuheng:latest
```

### 环境变量说明

| 变量名                     | 说明            | 默认值                                          |
| -------------------------- |---------------|----------------------------------------------|
| `SERVER_PORT`              | 服务端口          | `8520`                                       |
| `CONTEXT_PATH`             | 上下文路径         | `/yuheng-api`                                |
| `SPRING_REDIS_HOST`        | Redis 主机地址    | `xixi.vip`                                   |
| `SPRING_REDIS_PASSWORD`    | Redis 密码      | -                                            |
| `SPRING_DATASOURCE_URL`    | 元数据库 JDBC URL | `jdbc:mysql://titi.vip:3306/yuheng?characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | 元数据库用户名       | `root`                                       |
| `SPRING_DATASOURCE_PASSWORD` | 元数据库密码        | `123456`                                     |
| `SPRING_DATASOURCE_DRIVER_CLASS_NAME` | 元数据库驱动类       | `com.mysql.cj.jdbc.Driver`                   |

## Docker Compose 部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  yuheng-api:
    image: registry.cn-hangzhou.aliyuncs.com/tianxuan/yuheng:latest
    container_name: yuheng-api
    ports:
      - '8520:8520'
    environment:
      - SERVER_PORT=8520
      - CONTEXT_PATH=/yuheng-api
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PASSWORD=your-redis-password
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/yuheng?characterEncoding=utf-8&useSSL=false
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123456
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    container_name: yuheng-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=123456
      - MYSQL_DATABASE=yuheng
    ports:
      - '3306:3306'
    volumes:
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    container_name: yuheng-redis
    ports:
      - '6379:6379'

volumes:
  mysql-data:
```

```shell
docker-compose up -d
```

## Jar 包部署

### 1. 构建

```shell
./mvnw clean package -DskipTests
```

### 2. 运行

```shell
java -jar target/yuheng-api-0.0.1-SNAPSHOT.jar \
  --server.port=8520 \
  --spring.datasource.url=jdbc:mysql://localhost:3306/yuheng \
  --spring.datasource.username=root \
  --spring.datasource.password=123456
```

## 访问

部署成功后，访问：

用户名密码`admin/adminadmin`

- **管理端**：`http://localhost:8520/yuheng-api/`
- **API 接口**：`http://localhost:8520/yuheng-api/api/{path}`

## GraalVM Native Image（实验性）

项目支持编译为 GraalVM Native Image，实现毫秒级启动。

```shell
./mvnw -Pnative native:compile
```

编译完成后运行：

```shell
./target/yuheng-api
```
