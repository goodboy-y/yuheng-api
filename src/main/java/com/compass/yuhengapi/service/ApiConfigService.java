package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.bean.ApiParam;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;

import java.util.List;

public interface ApiConfigService {

    void delete(String id);

    ApiConfig detail(String id);

    PageList<ApiConfig> search(ApiConfigQueryCmd queryCmd);

    void online(String id);

    void offline(String id);

    ApiConfig getConfig(String path);

    List<ApiParam> getRequestParam(String sql);

    /**
     * 新增API并保存字段映射
     */
    String addWithMappings(ApiConfig apiConfig, List<ApiFieldMapping> fieldMappings);

    /**
     * 更新API并保存字段映射
     */
    void updateWithMappings(ApiConfig apiConfig, List<ApiFieldMapping> fieldMappings);

    /**
     * 获取未授权给指定客户端的API列表
     */
    PageList<ApiConfig> getUnAuthorizedApiConfigs(String clientId, int pageNum, int pageSize, String name);

}
