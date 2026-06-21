# 玉衡API

将sql直接转换为api接口

## 环境要求

JDK17+

## 安装部署

1.  docker 安装   
```shell
docker run -d --name=yuheng-api\
  -p 8520:8520 \
  -e SERVER_PORT=8520 \
  -e CONTEXT_PATH=/yuheng-api \
  -e SPRING_REDIS_HOST=xixi.vip \
  -e SPRING_REDIS_PASSWORD='xixiRedisMima1996!' \
  -e SPRING_DATASOURCE_URL='jdbc:oracle:thin:@172.17.0.1:1521/orclpdb1' \
  -e SPRING_DATASOURCE_USERNAME=eams_dev \
  -e SPRING_DATASOURCE_PASSWORD=eams \
  yuhengapi:1.0
```
2. jar包安装：略

## 如何使用

1. 先创建数据源
   ![](/doc/images/datasource-add.png)
2. 创建客户端
   ![](/doc/images/client-add.png)
3. 创建api接口
   ![](/doc/images/api-add.png)
4. 使用api
   ![](/doc/images/api-use.png)
   ![](/doc/images/api-use2.png)

## 客户端

[如何使用](./doc/usage.md)

## 如何贡献



