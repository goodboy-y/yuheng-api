package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import com.compass.yuhengapi.service.ApiConfigService;
import com.compass.yuhengapi.service.ApiDataSourceService;
import com.compass.yuhengapi.service.ApiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiConfigService apiConfigService;

    @Autowired
    private ApiDataSourceService dbDataSourceService;

    @Autowired
    private ApiService apiService;

    @RequestMapping("/{path}")
    public Result<Object> hello(@PathVariable("path") String path, HttpServletRequest request) {
        try {
            ApiConfig config = apiConfigService.getConfig(path);
            if (config == null) {
                return Result.fail("该接口不存在！！");
            }
            ApiDatasource datasource = dbDataSourceService.detail(config.getDatasource().getId());
            if (datasource == null) {
                return Result.fail("数据源不存在！！");
            }
            return apiService.executeSql(request, config, datasource);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

}
