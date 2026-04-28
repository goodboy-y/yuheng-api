package com.compass.yuhengapi.service.impl;

import com.alibaba.fastjson2.JSON;
import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.bean.ApiParam;
import com.compass.yuhengapi.model.bean.SqlParam;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.repo.ApiClientRepository;
import com.compass.yuhengapi.repo.ApiConfigRepository;
import com.compass.yuhengapi.repo.ApiFieldMappingRepository;
import com.compass.yuhengapi.service.ApiConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConfigServiceImpl implements ApiConfigService {

    private final ApiConfigRepository apiConfigRepository;
    private final ApiFieldMappingRepository fieldMappingRepository;
    private final ApiClientRepository apiClientRepository;

    private final Pattern pattern = Pattern.compile("#\\{([^}]*)}");

    private void parseParams(ApiConfig apiConfig) {
        String sqlParam = apiConfig.getSql_param();
        SqlParam sqlParamObj = JSON.parseObject(sqlParam, SqlParam.class);
        List<ApiParam> params = getApiParams(sqlParamObj.getSql());
        sqlParamObj.setParams(params);
        apiConfig.setSql_param(JSON.toJSONString(sqlParamObj));
    }

    /**
     * 从原始sql中获取参数
     */
    private List<ApiParam> getApiParams(String sqlParamObj) {
        Matcher matcher = pattern.matcher(sqlParamObj);
        List<String> placeholders = new ArrayList<>();
        while (matcher.find()) {
            // group(0) 返回整个匹配的内容，如 #{id}
            // group(1) 返回第一个捕获组的内容，即括号内的部分
            placeholders.add(matcher.group(1));
        }
        List<ApiParam> params = new ArrayList<>();
        for (String placeholder : placeholders) {
            ApiParam e = new ApiParam();
            e.setName(placeholder);
            if (placeholder.contains(":int")) {
                e.setType("number");
            } else if (placeholder.contains(":double")) {
                e.setType("double");
            } else {
                e.setType("string");
            }
            params.add(e);
        }
        return params;
    }

    @Override
    public List<ApiParam> getRequestParam(String sql) {
        return getApiParams(sql);
    }

    @Transactional
    public void delete(String id) {
        apiConfigRepository.deleteById(id);
    }

    @Override
    public ApiConfig detail(String id) {
        return apiConfigRepository.findById(id).orElse(null);
    }

    @Override
    public PageList<ApiConfig> search(ApiConfigQueryCmd queryCmd) {
        Specification<ApiConfig> spec = Specification.unrestricted();

        if (StringUtils.isNoneBlank(queryCmd.getName())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + queryCmd.getName() + "%"));
        }
        if (StringUtils.isNoneBlank(queryCmd.getPath())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("path"), queryCmd.getPath()));
        }
        if (Objects.nonNull(queryCmd.getStatus())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), queryCmd.getStatus()));
        }

        Pageable pageable = queryCmd.toPageable();
        Page<ApiConfig> all = apiConfigRepository.findAll(spec, pageable);
        return PageList.of(all, pageable);
    }

    @Override
    public ApiConfig getConfig(String path) {
        return apiConfigRepository.selectByPath(path);
    }

    @Override
    public void online(String id) {
        ApiConfig apiConfig = apiConfigRepository.findById(id).orElse(null);
        if (apiConfig == null) {
            return;
        }
        apiConfig.setStatus(1);
        apiConfigRepository.save(apiConfig);
    }

    @Override
    public void offline(String id) {
        ApiConfig apiConfig = apiConfigRepository.findById(id).orElse(null);
        if (apiConfig == null) {
            return;
        }
        apiConfig.setStatus(0);
        apiConfigRepository.save(apiConfig);
    }

    @Transactional
    @Override
    public String addWithMappings(ApiConfig apiConfig, List<com.compass.yuhengapi.model.entities.ApiFieldMapping> fieldMappings) {
        int size = apiConfigRepository.selectCountByPath(apiConfig.getPath());
        if (size > 0) {
            throw new RuntimeException("该路径已被使用，请修改请求路径再保存");
        } else {
            parseParams(apiConfig);
            apiConfigRepository.save(apiConfig);
            
            // 保存字段映射
            saveFieldMappings(apiConfig.getId(), fieldMappings);
            
            return apiConfig.getId();
        }
    }

    @Transactional
    @Override
    public void updateWithMappings(ApiConfig apiConfig, List<com.compass.yuhengapi.model.entities.ApiFieldMapping> fieldMappings) {
        int size = apiConfigRepository.selectCountByPathWhenUpdate(apiConfig.getPath(), apiConfig.getId());
        if (size > 0) {
            throw new RuntimeException("该路径已被使用，请修改请求路径再保存");
        } else {
            parseParams(apiConfig);
            apiConfigRepository.save(apiConfig);
            
            // 保存字段映射
            saveFieldMappings(apiConfig.getId(), fieldMappings);
        }
    }

    /**
     * 保存字段映射配置
     */
    private void saveFieldMappings(String apiConfigId, List<com.compass.yuhengapi.model.entities.ApiFieldMapping> fieldMappings) {
        if (fieldMappings != null && !fieldMappings.isEmpty()) {
            // 先删除旧的映射配置
            fieldMappingRepository.deleteByApiConfigId(apiConfigId);
            
            // 保存新的映射配置
            for (com.compass.yuhengapi.model.entities.ApiFieldMapping mapping : fieldMappings) {
                mapping.setId(null);
                mapping.setApiConfigId(apiConfigId);
            }
            
            fieldMappingRepository.saveAll(fieldMappings);
        } else {
            // 如果没有字段映射，则删除所有旧的映射
            fieldMappingRepository.deleteByApiConfigId(apiConfigId);
        }
    }

    @Override
    public PageList<ApiConfig> getUnAuthorizedApiConfigs(String clientId, int pageNum, int pageSize, String name) {
        // 验证客户端是否存在
        apiClientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("客户端不存在"));

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNum);
        Page<ApiConfig> page;

        if (StringUtils.isNoneBlank(name)) {
            page = apiConfigRepository.findUnAuthorizedApiConfigsByName(clientId, name, pageable);
        } else {
            page = apiConfigRepository.findUnAuthorizedApiConfigs(clientId, pageable);
        }

        return PageList.of(page, pageable);
    }

}
