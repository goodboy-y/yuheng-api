INSERT INTO api_plugin (id, name, description, class_name)
VALUES ('004216ed-a25b-4119-acee-4234c9b1f521', '分页插件',
        '为API添加分页功能，自动处理page和pageSize参数，并返回分页信息',
        'com.compass.yuhengapi.plugin.impl.PaginationPlugin');

-- 插入初始用户密码 admin/adminadmin
insert into api_account(username, password)
values ('admin','$2a$10$zPPeBr69r09HK1bQwDMHm.Df5r0SrdXcd6wnKyJD6tcOW35v9050G')