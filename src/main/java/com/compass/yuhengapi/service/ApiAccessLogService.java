package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiAccessLogQueryCmd;
import com.compass.yuhengapi.model.entities.ApiAccessLog;
import com.compass.yuhengapi.model.entities.ApiAccessLogArchive;

import java.time.LocalDateTime;
import java.util.Map;

public interface ApiAccessLogService {

    void saveLog(String path, String clientId, String params, String accountId);

    PageList<ApiAccessLog> search(ApiAccessLogQueryCmd queryCmd);

    void delete(String ids);

    int archive(ApiAccessLogQueryCmd queryCmd);

    PageList<ApiAccessLogArchive> getArchiveLogs(ApiAccessLogQueryCmd queryCmd);

    Map<String, Object> autoArchive();
}