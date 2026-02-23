package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.bean.ApiParam;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfig;

import java.util.List;

public interface ApiConfigService {

    void add(ApiConfig data);

    void update(ApiConfig data);

    void delete(String id);

    ApiConfig detail(String id);

    PageList<ApiConfig> search(ApiConfigQueryCmd queryCmd);

    void online(String id);

    void offline(String id);

    ApiConfig getConfig(String path);

    List<ApiParam> getRequestParam(String sql);

}
