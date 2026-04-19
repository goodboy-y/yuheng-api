package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiAccessLog;
import com.compass.yuhengapi.model.entities.ApiAccessLogArchive;
import com.compass.yuhengapi.service.ApiAccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/apiAccessLog")
public class ApiAccessLogController {

    private final ApiAccessLogService apiAccessLogService;

    @GetMapping("/search")
    public Result<PageList<ApiAccessLog>> search(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @RequestParam(required = false) String path,
        @RequestParam(required = false) String clientId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(apiAccessLogService.search(startTime, endTime, path, clientId, page, pageSize));
    }

    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String ids) {
        apiAccessLogService.delete(ids);
        return Result.success("删除成功");
    }

    @PostMapping("/archive")
    public Result<String> archive(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime beforeTime) {
        int count = apiAccessLogService.archive(beforeTime);
        return Result.success("归档成功，归档数量: " + count);
    }

    @GetMapping("/archive/search")
    public Result<PageList<ApiAccessLogArchive>> searchArchive(
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @RequestParam(required = false) String path,
        @RequestParam(required = false) String clientId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(apiAccessLogService.getArchiveLogs(startTime, endTime, path, clientId, page, pageSize));
    }
}