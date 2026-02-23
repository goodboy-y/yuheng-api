package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import jakarta.servlet.http.HttpServletRequest;

public interface ApiService {


    Result<Object> executeSql(HttpServletRequest request, ApiConfig config, ApiDatasource datasource);

}
