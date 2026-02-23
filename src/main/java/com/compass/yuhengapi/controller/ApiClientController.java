package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ApiClientQueryCmd;
import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.vo.ApiClientVo;
import com.compass.yuhengapi.service.impl.ApiClientServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api-client")
public class ApiClientController {

    @Autowired
    ApiClientServiceImpl dbApiClientServiceImpl;

    @RequestMapping("/search")
    public Result<PageList<ApiClient>> search(ApiClientQueryCmd queryCmd) {
        return Result.success(dbApiClientServiceImpl.search(queryCmd));
    }

    @RequestMapping("/add")
    public void add(@RequestBody ApiClient dataSource) {
        dbApiClientServiceImpl.add(dataSource);
    }

    @RequestMapping("/detail/{id}")
    public Result<ApiClientVo> detail(@PathVariable("id") String id) {
        return Result.success(dbApiClientServiceImpl.detail(id));
    }

    @RequestMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        dbApiClientServiceImpl.delete(id);
        return Result.success("删除成功");
    }

    @RequestMapping("/update")
    public Result<String> update(@RequestBody ApiClient dataSource) {
        dbApiClientServiceImpl.update(dataSource);
        return Result.success("更新成功");
    }

}
