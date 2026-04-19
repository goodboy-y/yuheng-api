package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiConfigAccessQueryCmd;
import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiConfigAccess;
import com.compass.yuhengapi.repo.ApiClientRepository;
import com.compass.yuhengapi.repo.ApiConfigAccessRepository;
import com.compass.yuhengapi.repo.ApiConfigRepository;
import com.compass.yuhengapi.service.ApiConfigAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConfigAccessServiceImpl implements ApiConfigAccessService {

    private final ApiConfigAccessRepository apiConfigAccessRepository;
    private final ApiClientRepository apiClientRepository;
    private final ApiConfigRepository apiConfigRepository;

    @Override
    @Transactional
    public void grant(String clientId, String apiConfigId) {
        int count = apiConfigAccessRepository.countByClientAndApiConfig(clientId, apiConfigId);
        if (count > 0) {
            throw new RuntimeException("该客户端已拥有此API的访问权限");
        }

        ApiClient client = apiClientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("客户端不存在"));
        ApiConfig apiConfig = apiConfigRepository.findById(apiConfigId)
                .orElseThrow(() -> new RuntimeException("API配置不存在"));

        ApiConfigAccess access = new ApiConfigAccess();
        access.setClient(client);
        access.setApiConfig(apiConfig);
        access.setAccountId(client.getAccountId());
        apiConfigAccessRepository.save(access);
    }

    @Override
    @Transactional
    public void revoke(String id) {
        apiConfigAccessRepository.deleteById(id);
    }

    @Override
    public ApiConfigAccess detail(String id) {
        return apiConfigAccessRepository.findById(id).orElse(null);
    }

    @Override
    public PageList<ApiConfigAccess> search(ApiConfigAccessQueryCmd queryCmd) {
        Specification<ApiConfigAccess> spec = Specification.unrestricted();

        if (StringUtils.isNoneBlank(queryCmd.getClientId())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("client").get("id"), queryCmd.getClientId()));
        }

        if (StringUtils.isNoneBlank(queryCmd.getApiConfigId())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("apiConfig").get("id"), queryCmd.getApiConfigId()));
        }

        if (StringUtils.isNoneBlank(queryCmd.getClientName())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("client").get("name"), "%" + queryCmd.getClientName() + "%"));
        }

        if (StringUtils.isNoneBlank(queryCmd.getApiPath())) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(root.get("apiConfig").get("path"), "%" + queryCmd.getApiPath() + "%"));
        }

        Pageable pageable = queryCmd.toPageable();
        Page<ApiConfigAccess> all = apiConfigAccessRepository.findAll(spec, pageable);
        return PageList.of(all, pageable);
    }

    @Override
    public boolean hasAccess(String clientId, String apiConfigId) {
        int count = apiConfigAccessRepository.countByClientAndApiConfig(clientId, apiConfigId);
        return count > 0;
    }
}
