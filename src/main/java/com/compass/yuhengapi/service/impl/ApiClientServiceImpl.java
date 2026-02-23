package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.converter.ApiClientConverter;
import com.compass.yuhengapi.model.dto.ApiClientQueryCmd;
import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.vo.ApiClientVo;
import com.compass.yuhengapi.repo.ApiClientRepository;
import com.compass.yuhengapi.service.ApiClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@AllArgsConstructor
public class ApiClientServiceImpl implements ApiClientService {

    private ApiClientRepository apiClientRepository;

    private ApiClientConverter apiClientConverter;

    @Override
    public void add(ApiClient data) {
        String a = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom secureRandom = new SecureRandom();
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .usingRandom(secureRandom::nextInt)
            .selectFrom(a.toCharArray())
            .get();
        String sec = generator.generate(64);
        String clientId = generator.generate(16);

        data.setClientId(clientId);
        data.setSecret(sec);
        apiClientRepository.save(data);
    }

    @Override
    public void update(ApiClient data) {
        apiClientRepository.save(data);
    }

    @Override
    public void delete(String id) {
        apiClientRepository.deleteById(id);
    }

    @Override
    public ApiClientVo detail(String id) {
        return apiClientConverter.toPageVo(apiClientRepository.findById(id).orElse(null));
    }

    @Override
    public PageList<ApiClient> search(ApiClientQueryCmd queryCmd) {
        Specification<ApiClient> spec = Specification.unrestricted();
        if (StringUtils.isNoneBlank(queryCmd.getName())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + queryCmd.getName() + "%"));
        }
        if (StringUtils.isNoneBlank(queryCmd.getClientId())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("clientId"), queryCmd.getClientId()));
        }
        Pageable pageable = queryCmd.toPageable();
        Page<ApiClient> all = apiClientRepository.findAll(spec, pageable);
        return PageList.of(all, pageable);
    }

}
