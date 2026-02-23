package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiDatasourceQueryCmd;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ApiDataSourceService {


    @Transactional
    void add(ApiDatasource dataSource);

    @Transactional
    void update(ApiDatasource dataSource);

    @Transactional
    void delete(String id);

    ApiDatasource detail(String id);

    PageList<ApiDatasource> search(ApiDatasourceQueryCmd queryCmd);

    List<ApiDatasource> list();
}
