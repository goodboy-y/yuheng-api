package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface ApiService {


    Result<Object> executeSql(HttpServletRequest request, ApiConfig config, ApiDatasource datasource);

    Result<Object> testApi(String apiId, Map<String, Object> params);

    Result<List<String>> parseSqlFields(String datasourceId, String sql);

}
