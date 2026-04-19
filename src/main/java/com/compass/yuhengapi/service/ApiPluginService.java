package com.compass.yuhengapi.service;

import com.compass.yuhengapi.model.entities.ApiPlugin;
import com.compass.yuhengapi.plugin.Plugin;

import java.util.List;
import java.util.Map;

public interface ApiPluginService {

    void initDefaultPlugins();

    List<ApiPlugin> getAllPlugins();

    ApiPlugin getPluginById(String id);

    Map<String, Plugin> loadPluginsByApiConfig(String apiConfigId);

    void saveApiConfigPlugins(String apiConfigId, List<String> pluginIds);

    List<ApiPlugin> getApiConfigPlugins(String apiConfigId);
}
