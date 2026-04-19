---
alwaysApply: true
---
改完代码后无需回复改了那些内容，只需要我检查一下代码是否符合规范即可
修改完java代码无需编译检查
本项目使用的是spring-boot 4.0版本,使用的是java 17版本,写的代码需符合spring-boot 4.0的规范
本项目使用的是前后端分离架构，前端使用的是vue3 + element-plus 组件库

如果修改有变动表字段的，请同时修改以下两个文件
- src/main/resources/db/mysql_create_tables.sql
- src/main/resources/db/oracle_create_tables.sql

目录结构
```
src 中是后端代码
web 中是前端代码
doc 中是项目文档
```
修改完前端代码需要重新编译通过后打包