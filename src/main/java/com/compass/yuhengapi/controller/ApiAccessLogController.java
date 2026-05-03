package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ApiAccessLogQueryCmd;
import com.compass.yuhengapi.model.entities.ApiAccessLog;
import com.compass.yuhengapi.model.entities.ApiAccessLogArchive;
import com.compass.yuhengapi.service.ApiAccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/apiAccessLog")
public class ApiAccessLogController {

    private final ApiAccessLogService apiAccessLogService;

    @GetMapping("/search")
    public Result<PageList<ApiAccessLog>> search(ApiAccessLogQueryCmd queryCmd) {
        return Result.success(apiAccessLogService.search(queryCmd));
    }

    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String ids) {
        apiAccessLogService.delete(ids);
        return Result.success("删除成功");
    }

    @PostMapping("/archive")
    public Result<String> archive(@RequestBody ApiAccessLogQueryCmd queryCmd) {
        int count = apiAccessLogService.archive(queryCmd);
        return Result.success("归档成功，归档数量: " + count);
    }

    @GetMapping("/archive/search")
    public Result<PageList<ApiAccessLogArchive>> searchArchive(ApiAccessLogQueryCmd queryCmd) {
        return Result.success(apiAccessLogService.getArchiveLogs(queryCmd));
    }
}