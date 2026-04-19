package com.compass.yuhengapi.plugin.impl;

import com.compass.yuhengapi.model.bean.ApiSql;
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
        return response;
    }

    @Override
    public ApiSql processSql(ApiSql apiSql, Map<String, Object> params, String databaseType) {
        int page = (int) params.getOrDefault(PAGE_PARAM, DEFAULT_PAGE);
        int pageSize = (int) params.getOrDefault(PAGE_SIZE_PARAM, DEFAULT_PAGE_SIZE);
        
        String originalSql = apiSql.sql();
        Object[] originalParams = apiSql.params();
        
        if ("mysql".equalsIgnoreCase(databaseType)) {
            return processMySqlPagination(originalSql, originalParams, page, pageSize);
        } else if ("oracle".equalsIgnoreCase(databaseType)) {
            return processOraclePagination(originalSql, originalParams, page, pageSize);
        } else {
            return apiSql;
        }
    }

    private ApiSql processMySqlPagination(String sql, Object[] params, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        String paginationSql = sql + " LIMIT ? OFFSET ?";
        
        Object[] newParams = new Object[params.length + 2];
        System.arraycopy(params, 0, newParams, 0, params.length);
        newParams[params.length] = pageSize;
        newParams[params.length + 1] = offset;
        
        return new ApiSql(paginationSql, newParams);
    }

    private ApiSql processOraclePagination(String sql, Object[] params, int page, int pageSize) {
        int startRow = (page - 1) * pageSize + 1;
        int endRow = startRow + pageSize - 1;
        
        String paginationSql = "SELECT * FROM (" +
                              "  SELECT t.*, ROWNUM as rn FROM (" +
                              sql +
                              ") t WHERE ROWNUM <= ?" +
                              ") WHERE rn >= ?";
        
        Object[] newParams = new Object[params.length + 2];
        System.arraycopy(params, 0, newParams, 0, params.length);
        newParams[params.length] = endRow;
        newParams[params.length + 1] = startRow;
        
        return new ApiSql(paginationSql, newParams);
    }
}
