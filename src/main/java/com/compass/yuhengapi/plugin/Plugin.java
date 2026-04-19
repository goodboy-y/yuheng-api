package com.compass.yuhengapi.plugin;

import com.compass.yuhengapi.model.bean.ApiSql;
import com.compass.yuhengapi.model.entities.ApiConfig;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface Plugin {

    String getName();

    String getDescription();

    void init(ApiConfig apiConfig);

    Map<String, Object> processRequest(HttpServletRequest request, Map<String, Object> params);

    Object processResponse(Object response);

    ApiSql processSql(ApiSql apiSql, Map<String, Object> params, String databaseType);
}
