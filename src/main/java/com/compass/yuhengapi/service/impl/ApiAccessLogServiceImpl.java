package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiAccessLogQueryCmd;
import com.compass.yuhengapi.model.entities.ApiAccessLog;
import com.compass.yuhengapi.model.entities.ApiAccessLogArchive;
import com.compass.yuhengapi.repo.ApiAccessLogArchiveRepository;
import com.compass.yuhengapi.repo.ApiAccessLogRepository;
import com.compass.yuhengapi.service.ApiAccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiAccessLogServiceImpl implements ApiAccessLogService {

    private final ApiAccessLogRepository apiAccessLogRepository;
    private final ApiAccessLogArchiveRepository apiAccessLogArchiveRepository;

    private static final int BATCH_SIZE = 500;

    @Override
    @Async
    public void saveLog(String path, String clientId, String params, String accountId) {
        try {
            ApiAccessLog log = new ApiAccessLog();
            log.setAccessTime(LocalDateTime.now());
            log.setPath(path);
            log.setClientId(clientId);
            log.setParams(params);
            log.setAccountId(accountId);
            apiAccessLogRepository.save(log);
        } catch (Exception e) {
            ApiAccessLogServiceImpl.log.error("保存API访问日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public PageList<ApiAccessLog> search(ApiAccessLogQueryCmd queryCmd) {
        Pageable pageable = queryCmd.toPageable();
        Specification<ApiAccessLog> spec = buildSpecification(queryCmd);
        Page<ApiAccessLog> result = apiAccessLogRepository.findAll(spec, pageable);
        return PageList.of(result, pageable);
    }

    @Override
    @Transactional
    public void delete(String ids) {
        if (ids == null || ids.isBlank()) {
            return;
        }
        List<String> idList = Arrays.asList(ids.split(","));
        apiAccessLogRepository.deleteAllById(idList);
    }

    @Override
    @Transactional
    public int archive(ApiAccessLogQueryCmd queryCmd) {
        LocalDateTime beforeTime = queryCmd.getBeforeTime();
        int totalArchived = 0;
        List<ApiAccessLog> batch = apiAccessLogRepository.findPageByAccessTimeBefore(beforeTime, PageRequest.of(0, BATCH_SIZE)).getContent();

        while (!batch.isEmpty()) {
            List<ApiAccessLogArchive> archives = batch.stream().map(this::convertToArchive).collect(Collectors.toList());
            apiAccessLogArchiveRepository.saveAll(archives);
            totalArchived += batch.size();

            List<String> logIds = batch.stream().map(ApiAccessLog::getId).collect(Collectors.toList());
            apiAccessLogRepository.deleteAllById(logIds);

            batch = apiAccessLogRepository.findPageByAccessTimeBefore(beforeTime, PageRequest.of(0, BATCH_SIZE)).getContent();
        }

        return totalArchived;
    }

    @Override
    public PageList<ApiAccessLogArchive> getArchiveLogs(ApiAccessLogQueryCmd queryCmd) {
        Pageable pageable = queryCmd.toPageable();
        Specification<ApiAccessLogArchive> spec = buildArchiveSpecification(queryCmd);
        Page<ApiAccessLogArchive> result = apiAccessLogArchiveRepository.findAll(spec, pageable);
        return PageList.of(result, pageable);
    }

    @Override
    @Transactional
    public Map<String, Object> autoArchive() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime beforeTime = LocalDateTime.now().minusMonths(3);
        ApiAccessLogQueryCmd queryCmd = new ApiAccessLogQueryCmd();
        queryCmd.setBeforeTime(beforeTime);
        try {
            int count = archive(queryCmd);
            result.put("success", true);
            result.put("archivedCount", count);
            result.put("beforeTime", beforeTime.toString());
        } catch (Exception e) {
            log.error("自动归档失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Specification<ApiAccessLog> buildSpecification(ApiAccessLogQueryCmd queryCmd) {
        Specification<ApiAccessLog> spec = Specification.unrestricted();

        if (queryCmd.getStartTime() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("accessTime"), queryCmd.getStartTime()));
        }
        if (queryCmd.getEndTime() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("accessTime"), queryCmd.getEndTime()));
        }
        if (StringUtils.isNoneBlank(queryCmd.getPath())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("path"), "%" + queryCmd.getPath() + "%"));
        }
        if (StringUtils.isNoneBlank(queryCmd.getClientId())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("clientId"), queryCmd.getClientId()));
        }

        return spec;
    }

    private Specification<ApiAccessLogArchive> buildArchiveSpecification(ApiAccessLogQueryCmd queryCmd) {
        Specification<ApiAccessLogArchive> spec = Specification.unrestricted();

        if (queryCmd.getStartTime() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("accessTime"), queryCmd.getStartTime()));
        }
        if (queryCmd.getEndTime() != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("accessTime"), queryCmd.getEndTime()));
        }
        if (StringUtils.isNoneBlank(queryCmd.getPath())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("path"), "%" + queryCmd.getPath() + "%"));
        }
        if (StringUtils.isNoneBlank(queryCmd.getClientId())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("clientId"), queryCmd.getClientId()));
        }

        return spec;
    }

    private ApiAccessLogArchive convertToArchive(ApiAccessLog log) {
        ApiAccessLogArchive archive = new ApiAccessLogArchive();
        archive.setId(log.getId());
        archive.setAccessTime(log.getAccessTime());
        archive.setPath(log.getPath());
        archive.setClientId(log.getClientId());
        archive.setParams(log.getParams());
        archive.setAccountId(log.getAccountId());
        return archive;
    }
}