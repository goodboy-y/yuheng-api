package com.compass.yuhengapi.task;

import com.compass.yuhengapi.service.ApiAccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAccessLogArchiveTask {

    private final ApiAccessLogService apiAccessLogService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void autoArchive() {
        log.info("开始执行API访问日志自动归档任务");
        try {
            Map<String, Object> result = apiAccessLogService.autoArchive();
            if (Boolean.TRUE.equals(result.get("success"))) {
                log.info("API访问日志自动归档任务完成，归档数量: {}", result.get("archivedCount"));
            } else {
                log.error("API访问日志自动归档任务失败: {}", result.get("error"));
            }
        } catch (Exception e) {
            log.error("API访问日志自动归档任务异常: {}", e.getMessage(), e);
        }
    }
}