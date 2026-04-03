---
alwaysApply: true
---
修改完java代码无需编译检查
本项目使用的是spring-boot 4.0版本,使用的是java 17版本,写的代码需符合spring-boot 4.0的规范
本项目使用的是前后端分离架构，前端使用的是vue3 + element-plus 组件库

目录结构
```
src 中是后端代码
web 中是前端代码
doc 中是项目文档
```
修改完前端代码需要重新编译通过后打包，并将打包后的文件替换到 src/main/resources/static 目录下