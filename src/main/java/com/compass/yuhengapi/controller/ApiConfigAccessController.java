package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ApiConfigAccessQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfigAccess;
import com.compass.yuhengapi.service.ApiConfigAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/apiConfigAccess")
public class ApiConfigAccessController {

    private final ApiConfigAccessService apiConfigAccessService;

    @RequestMapping("/search")
    public Result<PageList<ApiConfigAccess>> search(ApiConfigAccessQueryCmd queryCmd) {
        return Result.success(apiConfigAccessService.search(queryCmd));
    }

    @RequestMapping("/grant")
    public Result<String> grant(@RequestBody Map<String, String> params) {
        String clientId = params.get("clientId");
        String apiConfigId = params.get("apiConfigId");
        apiConfigAccessService.grant(clientId, apiConfigId);
        return Result.success("授权成功");
    }

    @RequestMapping("/revoke/{id}")
    public Result<String> revoke(@PathVariable("id") String id) {
        apiConfigAccessService.revoke(id);
        return Result.success("撤销授权成功");
    }

    @RequestMapping("/detail/{id}")
    public Result<ApiConfigAccess> detail(@PathVariable("id") String id) {
        return Result.success(apiConfigAccessService.detail(id));
    }
}
