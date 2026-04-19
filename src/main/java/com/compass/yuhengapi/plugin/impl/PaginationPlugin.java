package com.compass.yuhengapi.plugin.impl;

import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.plugin.Plugin;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

public class PaginationPlugin implements Plugin {

    private static final String PAGE_PARAM = "page";
    private static final String PAGE_SIZE_PARAM = "pageSize";
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private ApiConfig apiConfig;

    @Override
    public String getName() {
        return "分页插件";
    }

    @Override
    public String getDescription() {
        return "为API添加分页功能，自动处理page和pageSize参数，并返回分页信息";
    }

    @Override
    public void init(ApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    @Override
    public Map<String, Object> processRequest(HttpServletRequest request, Map<String, Object> params) {
        Map<String, Object> processedParams = new HashMap<>(params);
        
        try {
            int page = Integer.parseInt(request.getParameter(PAGE_PARAM));
            if (page < 1) {
                page = DEFAULT_PAGE;
            }
            processedParams.put(PAGE_PARAM, page);
        } catch (NumberFormatException e) {
            processedParams.put(PAGE_PARAM, DEFAULT_PAGE);
        }

        try {
            int pageSize = Integer.parseInt(request.getParameter(PAGE_SIZE_PARAM));
            if (pageSize < 1) {
                pageSize = DEFAULT_PAGE_SIZE;
            }
            if (pageSize > MAX_PAGE_SIZE) {
                pageSize = MAX_PAGE_SIZE;
            }
            processedParams.put(PAGE_SIZE_PARAM, pageSize);
        } catch (NumberFormatException e) {
            processedParams.put(PAGE_SIZE_PARAM, DEFAULT_PAGE_SIZE);
        }

        return processedParams;
    }

    @Override
    public Object processResponse(Object response) {
        if (response instanceof Map) {
            Map<String, Object> responseMap = (Map<String, Object>) response;
            if (responseMap.containsKey("data") && responseMap.get("data") instanceof java.util.List) {
                java.util.List<?> data = (java.util.List<?>) responseMap.get("data");
                
                Map<String, Object> pagination = new HashMap<>();
                pagination.put("total", data.size());
                pagination.put("page", responseMap.getOrDefault(PAGE_PARAM, DEFAULT_PAGE));
                pagination.put("pageSize", responseMap.getOrDefault(PAGE_SIZE_PARAM, DEFAULT_PAGE_SIZE));
                pagination.put("totalPages", (int) Math.ceil((double) data.size() / (int) responseMap.getOrDefault(PAGE_SIZE_PARAM, DEFAULT_PAGE_SIZE)));
                
                responseMap.put("pagination", pagination);
            }
        }
        return response;
    }
}
