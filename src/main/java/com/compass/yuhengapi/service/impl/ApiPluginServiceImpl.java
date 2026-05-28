package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiConfigPlugin;
import com.compass.yuhengapi.model.entities.ApiPlugin;
import com.compass.yuhengapi.plugin.Plugin;
import com.compass.yuhengapi.plugin.impl.PaginationPlugin;
import com.compass.yuhengapi.repo.ApiConfigPluginRepository;
import com.compass.yuhengapi.repo.ApiConfigRepository;
import com.compass.yuhengapi.repo.ApiPluginRepository;
import com.compass.yuhengapi.service.ApiPluginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiPluginServiceImpl implements ApiPluginService {

    private final ApiPluginRepository apiPluginRepository;
    private final ApiConfigRepository apiConfigRepository;
    private final ApiConfigPluginRepository apiConfigPluginRepository;

    private final Map<String, Plugin> pluginCache = new HashMap<>();

    @Override
    public void initDefaultPlugins() {
        if (apiPluginRepository.findByName("分页插件") == null) {
            ApiPlugin paginationPlugin = new ApiPlugin();
            paginationPlugin.setName("分页插件");
            paginationPlugin.setDescription("为API添加分页功能，自动处理page和pageSize参数，并返回分页信息");
            paginationPlugin.setClassName(PaginationPlugin.class.getSimpleName());
            apiPluginRepository.save(paginationPlugin);
            log.info("初始化分页插件成功");
        }
    }

    @Override
    public List<ApiPlugin> getAllPlugins() {
        return apiPluginRepository.findAll();
    }

    @Override
    public ApiPlugin getPluginById(String id) {
        return apiPluginRepository.findById(id).orElse(null);
    }

    @Override
    public Map<String, Plugin> loadPluginsByApiConfig(String apiConfigId) {
        Map<String, Plugin> plugins = new HashMap<>();
        ApiConfig apiConfig = apiConfigRepository.findById(apiConfigId).orElse(null);
        if (apiConfig == null) {
            return plugins;
        }

        List<ApiConfigPlugin> apiConfigPlugins = apiConfigPluginRepository.findByApiConfig(apiConfig);
        for (ApiConfigPlugin apiConfigPlugin : apiConfigPlugins) {
            ApiPlugin apiPlugin = apiConfigPlugin.getApiPlugin();
            if (apiPlugin == null) {
                continue;
            }
            Plugin plugin = loadPlugin(apiPlugin);
            if (plugin == null) {
                continue;
            }
            plugin.init(apiConfig);
            plugins.put(apiPlugin.getId(), plugin);
        }

        return plugins;
    }

    @Override
    @Transactional
    public void saveApiConfigPlugins(String apiConfigId, List<String> pluginIds) {
        ApiConfig apiConfig = apiConfigRepository.findById(apiConfigId).orElse(null);
        if (apiConfig == null) {
            return;
        }

        apiConfigPluginRepository.deleteByApiConfig(apiConfig);

        for (String pluginId : pluginIds) {
            ApiPlugin apiPlugin = apiPluginRepository.findById(pluginId).orElse(null);
            if (apiPlugin != null) {
                ApiConfigPlugin apiConfigPlugin = new ApiConfigPlugin();
                apiConfigPlugin.setApiConfig(apiConfig);
                apiConfigPlugin.setApiPlugin(apiPlugin);
                apiConfigPlugin.setAccountId(apiConfig.getAccountId());
                apiConfigPluginRepository.save(apiConfigPlugin);
            }
        }
    }

    @Override
    public List<ApiPlugin> getApiConfigPlugins(String apiConfigId) {
        List<ApiPlugin> plugins = new ArrayList<>();
        ApiConfig apiConfig = apiConfigRepository.findById(apiConfigId).orElse(null);
        if (apiConfig == null) {
            return plugins;
        }

        List<ApiConfigPlugin> apiConfigPlugins = apiConfigPluginRepository.findByApiConfig(apiConfig);
        for (ApiConfigPlugin apiConfigPlugin : apiConfigPlugins) {
            plugins.add(apiConfigPlugin.getApiPlugin());
        }

        return plugins;
    }

    private Plugin loadPlugin(ApiPlugin apiPlugin) {
        try {
            String className = apiPlugin.getClassName();
            if (pluginCache.containsKey(className)) {
                return pluginCache.get(className);
            }

            Class<?> pluginClass = Class.forName(className);
            Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();
            pluginCache.put(className, plugin);
            return plugin;
        } catch (Exception e) {
            log.error("加载插件失败: {}", e.getMessage(), e);
            return null;
        }
    }
}
