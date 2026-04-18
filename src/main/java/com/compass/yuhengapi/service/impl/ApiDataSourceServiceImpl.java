package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.common.lang.APIException;
import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiDatasourceQueryCmd;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import com.compass.yuhengapi.repo.ApiConfigRepository;
import com.compass.yuhengapi.repo.ApiDataSourceRepository;
import com.compass.yuhengapi.service.ApiDataSourceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class ApiDataSourceServiceImpl implements ApiDataSourceService {

    private ApiDataSourceRepository apiDataSourceRepository;

    private ApiConfigRepository apiConfigRepository;

    @Transactional
    @Override
    public void add(ApiDatasource dataSource) {
        apiDataSourceRepository.save(dataSource);
    }

    @Transactional
    @Override
    public void update(ApiDatasource dataSource) {
        // 注销旧的连接池
        com.compass.yuhengapi.util.PoolManager.removePool(dataSource.getId());
        apiDataSourceRepository.save(dataSource);
    }

    @Transactional
    @Override
    public void delete(String id) {
        int i = apiConfigRepository.countByDatasource(id);
        if (i == 0) {
            apiDataSourceRepository.deleteById(id);
        } else {
            throw new APIException("该数据源已经被使用，不可删除");
        }
    }

    @Override
    public ApiDatasource detail(String id) {
        return apiDataSourceRepository.findById(id).orElse(null);
    }

    @Override
    public PageList<ApiDatasource> search(ApiDatasourceQueryCmd queryCmd) {

        Specification<ApiDatasource> spec = Specification.unrestricted();
        if (StringUtils.isNoneBlank(queryCmd.getName())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + queryCmd.getName() + "%"));
        }
        if (StringUtils.isNoneBlank(queryCmd.getType())) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("type"), queryCmd.getName()));
        }

        Pageable pageable = queryCmd.toPageable();
        Page<ApiDatasource> all = apiDataSourceRepository.findAll(spec, pageable);
        return PageList.of(all, pageable);
    }

    @Override
    public List<ApiDatasource> list() {
        return apiDataSourceRepository.findByAccountId(null);
    }

}
