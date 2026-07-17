package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiConfigDetailDto;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;

import java.util.List;

public interface ApiConfigService {

    void delete(String id);

    ApiConfig detail(String id);

    PageList<ApiConfig> search(ApiConfigQueryCmd queryCmd);

    ApiConfig getConfig(String path);

    /**
     * 新增API并保存字段映射和插件配置
     */
    String addWithMappings(ApiConfig apiConfig, List<ApiFieldMapping> fieldMappings, List<String> pluginIds);

    /**
     * 更新API并保存字段映射和插件配置
     */
    void updateWithMappings(ApiConfig apiConfig, List<ApiFieldMapping> fieldMappings, List<String> pluginIds);

    /**
     * 获取未授权给指定客户端的API列表
     */
    PageList<ApiConfig> getUnAuthorizedApiConfigs(String clientId, int pageNum, int pageSize, String name);

    /**
     * 获取API详情，包括字段映射和插件配置
     */
    ApiConfigDetailDto getApiConfigDetail(String apiConfigId);

}
