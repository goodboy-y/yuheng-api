package com.compass.yuhengapi.controller;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.compass.yuhengapi.common.util.IpUtils;
import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.bean.ApiParam;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.service.ApiConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/apiConfig")
public class ApiConfigController {

    @Autowired
    ApiConfigService apiConfigService;

    @RequestMapping("/search")
    public Result<PageList<ApiConfig>> search(ApiConfigQueryCmd queryCmd) {
        return Result.success(apiConfigService.search(queryCmd));
    }

    @RequestMapping("/add")
    public Result<String> add(@RequestBody ApiConfig apiConfig) {
        apiConfigService.add(apiConfig);
        return Result.success("添加成功");
    }

    @RequestMapping("/parse-param")
    public Result<List<ApiParam>> parseParam(String sql) {
        return Result.success(apiConfigService.getRequestParam(sql));
    }

    @RequestMapping("/detail/{id}")
    public Result<ApiConfig> detail(@PathVariable("id") String id) {
        return Result.success(apiConfigService.detail(id));
    }

    @RequestMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        apiConfigService.delete(id);
        return Result.success("删除成功");
    }

    @RequestMapping("/update")
    public Result<String> update(@RequestBody ApiConfig apiConfig) {
        apiConfigService.update(apiConfig);
        return Result.success("修改成功");
    }

    @RequestMapping("/online/{id}")
    public void online(@PathVariable("id") String id) {
        apiConfigService.online(id);
    }

    @RequestMapping("/offline/{id}")
    public void offline(@PathVariable("id") String id) {
        apiConfigService.offline(id);
    }

    @Value("${server.port}")
    private String port;

    @RequestMapping("/get-base-url")
    public String getBaseUrl() {
        String hostIp = IpUtils.getHostIp();
        return hostIp + ":" + port;
    }

    @SuppressWarnings("all")
    @RequestMapping("/remote-request")
    public JSONObject request(String url, String params) {
        Map<String, Object> map = JSON.parseObject(params, Map.class);
        String post = HttpUtil.post(url, map);
        return JSON.parseObject(post);
    }

}
