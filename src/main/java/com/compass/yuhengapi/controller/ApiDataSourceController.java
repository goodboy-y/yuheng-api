package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ApiDatasourceQueryCmd;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import com.compass.yuhengapi.service.ApiDataSourceService;
import com.compass.yuhengapi.util.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/datasource")
public class ApiDataSourceController {

    @Autowired
    private ApiDataSourceService apiDataSourceService;

    @RequestMapping("/search")
    public Result<PageList<ApiDatasource>> search(ApiDatasourceQueryCmd queryCmd) {
        return Result.success(apiDataSourceService.search(queryCmd));
    }

    @RequestMapping("/add")
    public void add(@RequestBody ApiDatasource dataSource) {
        apiDataSourceService.add(dataSource);
    }

    @RequestMapping("/detail/{id}")
    public Result<ApiDatasource> detail(@PathVariable("id") String id) {
        return Result.success(apiDataSourceService.detail(id));
    }

    @RequestMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        apiDataSourceService.delete(id);
        return Result.success("删除成功");
    }

    @RequestMapping("/update")
    public Result<String> update(@RequestBody ApiDatasource dataSource) {
        apiDataSourceService.update(dataSource);
        return Result.success("更新成功");
    }

    @RequestMapping("/connect")
    public Result<String> connect(@RequestBody ApiDatasource dataSource) {
        try (Connection ignored = JdbcUtil.getConnection(dataSource)) {
            return Result.success("数据源连接成功");
        } catch (Exception e) {
            log.error(e.getMessage());
            return Result.fail(e.getMessage());
        }
    }

    @RequestMapping("/list")
    public Result<List<ApiDatasource>> list() {
        return Result.success(apiDataSourceService.list());
    }


}
