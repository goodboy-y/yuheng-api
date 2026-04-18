package com.compass.yuhengapi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.compass.yuhengapi.common.lang.APIException;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.bean.ApiSql;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import com.compass.yuhengapi.service.ApiConfigService;
import com.compass.yuhengapi.service.ApiDataSourceService;
import com.compass.yuhengapi.service.ApiService;
import com.compass.yuhengapi.util.JdbcUtil;
import com.compass.yuhengapi.util.PoolManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {
    
    private final ApiConfigService apiConfigService;
    private final ApiDataSourceService apiDataSourceService;

    @Override
    public Result<Object> executeSql(HttpServletRequest request, ApiConfig config, ApiDatasource datasource) {
        ApiSql apiSql = buildSqlFromRequest(request, config);
        if (apiSql == null) {
            return Result.custom(ReturnCodeEnum.RC400.getCode(), ReturnCodeEnum.RC400.getMessage(), null);
        }
        return executeSql(apiSql, datasource);
    }

    @Override
    public Result<Object> testApi(String apiId, Map<String, Object> params) {
        try {
            // 获取API配置
            ApiConfig config = apiConfigService.detail(apiId);
            if (config == null) {
                return Result.fail("该接口不存在！！");
            }
            
            // 获取数据源配置
            ApiDatasource datasource = apiDataSourceService.detail(config.getDatasource().getId());
            if (datasource == null) {
                return Result.fail("数据源不存在！！");
            }
            
            // 构建SQL
            ApiSql apiSql = buildSqlFromParams(params, config);
            if (apiSql == null) {
                return Result.custom(ReturnCodeEnum.RC400.getCode(), ReturnCodeEnum.RC400.getMessage(), null);
            }
            
            // 执行SQL
            return executeSql(apiSql, datasource);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail(e.getMessage());
        }
    }

    private ApiSql buildSqlFromRequest(HttpServletRequest request, ApiConfig config) {
        JSONObject jsonObject = JSON.parseObject(config.getSql_param());
        String sql = jsonObject.getString("sql");
        JSONArray requestParams = jsonObject.getJSONArray("params");
        if (requestParams == null) {
            return new ApiSql(sql, new Object[]{});
        }
        Object[] params = new Object[requestParams.size()];
        for (int i = 0; i < requestParams.size(); i++) {
            JSONObject jo = requestParams.getJSONObject(i);
            String name = jo.getString("name");
            String type = jo.getString("type");
            String old = "#{" + name + "}";
            sql = sql.replace(old, "?");
            String value = request.getParameter(StringUtils.substringBefore(name, ":"));
            if (value == null) {
                return null;
            }
            if ("int".equals(type)) {
                params[i] = Long.parseLong(value);
            } else if ("double".equals(type)) {
                params[i] = Double.parseDouble(value);
            } else {
                params[i] = value;
            }
        }
        return new ApiSql(sql, params);
    }
    
    private ApiSql buildSqlFromParams(Map<String, Object> params, ApiConfig config) {
        JSONObject jsonObject = JSON.parseObject(config.getSql_param());
        String sql = jsonObject.getString("sql");
        JSONArray requestParams = jsonObject.getJSONArray("params");
        if (requestParams == null) {
            return new ApiSql(sql, new Object[]{});
        }
        Object[] sqlParams = new Object[requestParams.size()];
        for (int i = 0; i < requestParams.size(); i++) {
            JSONObject jo = requestParams.getJSONObject(i);
            String name = jo.getString("name");
            String type = jo.getString("type");
            String old = "#{" + name + "}";
            sql = sql.replace(old, "?");
            
            // 获取参数值
            Object value = params.get(StringUtils.substringBefore(name, ":"));
            if (value == null) {
                return null;
            }
            
            // 转换参数类型
            if ("int".equals(type)) {
                sqlParams[i] = Long.parseLong(value.toString());
            } else if ("double".equals(type)) {
                sqlParams[i] = Double.parseDouble(value.toString());
            } else {
                sqlParams[i] = value.toString();
            }
        }
        return new ApiSql(sql, sqlParams);
    }
    
    private Result<Object> executeSql(ApiSql apiSql, ApiDatasource datasource) {
        try (DruidPooledConnection connection = PoolManager.getPooledConnection(datasource)) {
            ResultSet rs = JdbcUtil.query(apiSql.sql(), connection, apiSql.params());
            int columnCount = rs.getMetaData().getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rs.getMetaData().getColumnLabel(i);
                columns.add(columnName);
            }
            List<JSONObject> list = new ArrayList<>();
            while (rs.next()) {
                JSONObject jo = new JSONObject();
                columns.forEach(t -> {
                    try {
                        Object value = rs.getObject(t);
                        jo.put(StrUtil.toCamelCase(t.toLowerCase()), value);
                    } catch (SQLException e) {
                        log.error(ExceptionUtils.getStackTrace(e));
                    }
                });
                list.add(jo);
            }
            return Result.success(list);
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail(e.getMessage());
        } catch (APIException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail(e.getMessage());
        }
    }

    @Override
    public Result<List<String>> parseSqlFields(String datasourceId, String sql) {
        try {
            ApiDatasource datasource = apiDataSourceService.detail(datasourceId);
            if (datasource == null) {
                return Result.fail("数据源不存在");
            }

            // 获取一个连接来获取元数据，不实际执行查询
            try (DruidPooledConnection connection = PoolManager.getPooledConnection(datasource)) {
                // 使用 Druid 的 Util 来获取查询的元数据信息
                // 将 SQL 包装成 SELECT * FROM (...) AS t WHERE 1=0 的形式来获取字段信息
                String wrappedSql = wrapSqlForMetadata(sql);
                ResultSet rs = JdbcUtil.query(wrappedSql, connection, new Object[]{});
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.add(StrUtil.toCamelCase(metaData.getColumnLabel(i)));
                }
                return Result.success(columns);
            }
        } catch (SQLException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail("解析SQL字段失败：" + e.getMessage());
        } catch (APIException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail("解析SQL字段失败：" + e.getMessage());
        }
    }

    private String wrapSqlForMetadata(String sql) {
        // 移除末尾的分号
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        // 如果 SQL 包含参数占位符，替换为默认值或 1=1 条件
        // 由于我们只需要获取字段信息，不需要实际执行，所以使用 1=0 条件不返回任何数据
        sql = sql.replaceAll("#\\{[^}]+}", "1");

        // 添加 1=0 条件，确保不返回实际数据，只获取字段信息
        if (sql.toLowerCase().contains("where")) {
            sql = sql + " AND 1=0";
        } else {
            sql = sql + " WHERE 1=0";
        }

        return sql;
    }

}
