package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.entities.ApiAccessLog;
import com.compass.yuhengapi.model.entities.ApiAccessLogArchive;

import java.time.LocalDateTime;
import java.util.Map;

public interface ApiAccessLogService {

    void saveLog(String path, String clientId, String params, String accountId);

    PageList<ApiAccessLog> search(LocalDateTime startTime, LocalDateTime endTime, String path, String clientId, int page, int pageSize);

    void delete(String ids);

    int archive(LocalDateTime beforeTime);

    PageList<ApiAccessLogArchive> getArchiveLogs(LocalDateTime startTime, LocalDateTime endTime, String path, String clientId, int page, int pageSize);

    Map<String, Object> autoArchive();
}