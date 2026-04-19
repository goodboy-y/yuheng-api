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
            // 先执行count查询检查数据量
            long count = countRows(apiSql, connection);
            if (count > 1000) {
                return Result.fail("此语句数据量过大，请优化语句或改写为分页查询");
            }
            
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
    
    private long countRows(ApiSql apiSql, DruidPooledConnection connection) {
        String sql = apiSql.sql();
        String countSql = "SELECT COUNT(*) FROM (" + sql + ")";
        try (ResultSet rs = JdbcUtil.query(countSql, connection, apiSql.params())) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error("Count query failed: {}", ExceptionUtils.getStackTrace(e));
        }
        return 0;
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
                    columns.add(StrUtil.toCamelCase(metaData.getColumnLabel(i).toLowerCase()));
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

        // 查找 ORDER BY、GROUP BY、HAVING 等子句的位置
        int orderByIndex = sql.toLowerCase().indexOf(" order by");
        int groupByIndex = sql.toLowerCase().indexOf(" group by");
        int havingIndex = sql.toLowerCase().indexOf(" having");
        
        // 找到第一个出现的子句位置
        int clauseIndex = -1;
        if (orderByIndex != -1) clauseIndex = orderByIndex;
        if (groupByIndex != -1 && (clauseIndex == -1 || groupByIndex < clauseIndex)) clauseIndex = groupByIndex;
        if (havingIndex != -1 && (clauseIndex == -1 || havingIndex < clauseIndex)) clauseIndex = havingIndex;

        // 检查是否包含 WHERE 子句
        int whereIndex = sql.toLowerCase().indexOf("where");
        
        // 根据 SQL 结构添加 1=0 条件
        if (whereIndex != -1) {
            // 如果已经有 WHERE 子句，在 WHERE 子句后添加 AND 1=0
            if (clauseIndex != -1 && whereIndex < clauseIndex) {
                // 在 WHERE 子句和其他子句之间添加 AND 1=0
                String beforeClause = sql.substring(0, clauseIndex);
                String afterClause = sql.substring(clauseIndex);
                sql = beforeClause + " AND 1=0" + afterClause;
            } else {
                // 在末尾添加 AND 1=0
                sql = sql + " AND 1=0";
            }
        } else {
            // 如果没有 WHERE 子句，在其他子句之前添加 WHERE 1=0
            if (clauseIndex != -1) {
                String beforeClause = sql.substring(0, clauseIndex);
                String afterClause = sql.substring(clauseIndex);
                sql = beforeClause + " WHERE 1=0" + afterClause;
            } else {
                // 在末尾添加 WHERE 1=0
                sql = sql + " WHERE 1=0";
            }
        }

        return sql;
    }

}
