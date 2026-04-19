package com.compass.yuhengapi.service.impl;

import com.alibaba.fastjson2.JSON;
import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.bean.ApiParam;
import com.compass.yuhengapi.model.bean.SqlParam;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.repo.ApiConfigRepository;
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

    private final Pattern pattern = Pattern.compile("#\\{([^}]*)}");

    @Transactional
    public void add(ApiConfig apiConfig) {
        int size = apiConfigRepository.selectCountByPath(apiConfig.getPath());
        if (size > 0) {
            throw new RuntimeException("该路径已被使用，请修改请求路径再保存");
        } else {
            parseParams(apiConfig);
            apiConfigRepository.save(apiConfig);
        }
    }

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
    public void update(ApiConfig apiConfig) {
        int size = apiConfigRepository.selectCountByPathWhenUpdate(apiConfig.getPath(), apiConfig.getId());
        if (size > 0) {
            throw new RuntimeException("该路径已被使用，请修改请求路径再保存");
        } else {
            parseParams(apiConfig);
            apiConfigRepository.save(apiConfig);
        }
    }

    @Transactional
    public void delete(String id) {
        apiConfigRepository.deleteById(id);
    }

    public ApiConfig detail(String id) {
        return apiConfigRepository.findById(id).orElse(null);
    }

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

}
