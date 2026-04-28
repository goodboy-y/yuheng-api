package com.compass.yuhengapi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.compass.yuhengapi.common.lang.APIException;
import com.compass.yuhengapi.common.util.PageInfo;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.bean.ApiSql;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import com.compass.yuhengapi.service.ApiConfigService;
import com.compass.yuhengapi.service.ApiDataSourceService;
import com.compass.yuhengapi.service.ApiPluginService;
import com.compass.yuhengapi.service.ApiService;
import com.compass.yuhengapi.util.JdbcUtil;
import com.compass.yuhengapi.util.PoolManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

    private final ApiConfigService apiConfigService;
    private final ApiDataSourceService apiDataSourceService;
    private final ApiPluginService apiPluginService;

    @Override
    public Result<Object> executeSql(HttpServletRequest request, ApiConfig config, ApiDatasource datasource) {
        // 加载插件
        Map<String, com.compass.yuhengapi.plugin.Plugin> plugins = apiPluginService.loadPluginsByApiConfig(config.getId());

        // 处理请求参数
        Map<String, Object> requestParams = new java.util.HashMap<>();
        for (java.util.Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements(); ) {
            String name = paramNames.nextElement();
            requestParams.put(name, request.getParameter(name));
        }

        for (com.compass.yuhengapi.plugin.Plugin plugin : plugins.values()) {
            requestParams = plugin.processRequest(request, requestParams);
        }

        ApiSql apiSql = buildSqlFromRequest(request, config);
        if (apiSql == null) {
            return Result.custom(ReturnCodeEnum.RC400.getCode(), ReturnCodeEnum.RC400.getMessage(), null);
        }

        Result<Object> result = executeSql(apiSql, datasource, requestParams, plugins);

        // 处理响应
        if (result.getData() != null) {
            for (com.compass.yuhengapi.plugin.Plugin plugin : plugins.values()) {
                Object processedData = plugin.processResponse(result.getData());
                result.setData(processedData);
            }
        }

        return result;
    }

    @Override
    public Result<Object> testApi(String apiId, Map<String, Object> params) {
        try {
            // 获取API配置
            ApiConfig config = apiConfigService.detail(apiId);
            if (config == null) {
                return Result.fail("该接口不存在！！");
            }
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();


            // 加载插件
            Map<String, com.compass.yuhengapi.plugin.Plugin> plugins = apiPluginService.loadPluginsByApiConfig(config.getId());

            // 处理请求参数
            for (com.compass.yuhengapi.plugin.Plugin plugin : plugins.values()) {
                params = plugin.processRequest(attributes.getRequest(), params);
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
            Result<Object> result = executeSql(apiSql, datasource, params, plugins);

            // 处理响应
            if (result.getData() != null) {
                for (com.compass.yuhengapi.plugin.Plugin plugin : plugins.values()) {
                    Object processedData = plugin.processResponse(result.getData());
                    result.setData(processedData);
                }
            }

            return result;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 参数类型转换
     */
    private Object convertParamValue(Object value, String type) {
        if ("int".equals(type)) {
            return Long.parseLong(value.toString());
        } else if ("double".equals(type)) {
            return Double.parseDouble(value.toString());
        }
        return value.toString();
    }

    /**
     * 构建 SQL 语句和参数（核心方法）
     * @param config API 配置
     * @param paramProvider 参数值提供函数，输入参数名，返回参数值
     * @return 构建好的 SQL 和参数，如果参数缺失则返回 null
     */
    private ApiSql buildSql(ApiConfig config, Function<String, Object> paramProvider) {
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
            
            // 替换占位符
            sql = sql.replace("#{" + name + "}", "?");
            
            // 获取参数值（通过函数式参数）
            String paramName = StringUtils.substringBefore(name, ":");
            Object value = paramProvider.apply(paramName);
            if (value == null) {
                return null;
            }
            
            // 类型转换
            params[i] = convertParamValue(value, type);
        }
        return new ApiSql(sql, params);
    }

    private ApiSql buildSqlFromRequest(HttpServletRequest request, ApiConfig config) {
        return buildSql(config, request::getParameter);
    }

    private ApiSql buildSqlFromParams(Map<String, Object> params, ApiConfig config) {
        return buildSql(config, params::get);
    }

    private Result<Object> executeSql(ApiSql apiSql, ApiDatasource datasource, Map<String, Object> requestParams, Map<String, com.compass.yuhengapi.plugin.Plugin> plugins) {
        try (DruidPooledConnection connection = PoolManager.getPooledConnection(datasource)) {
            // 检查是否有分页插件
            boolean hasPaginationPlugin = plugins.values().stream()
                .anyMatch(p -> p instanceof com.compass.yuhengapi.plugin.impl.PaginationPlugin);

            // 应用插件处理SQL
            ApiSql processedApiSql = apiSql;
            for (com.compass.yuhengapi.plugin.Plugin plugin : plugins.values()) {
                processedApiSql = plugin.processSql(processedApiSql, requestParams, datasource.getType());
            }

            ResultSet rs = JdbcUtil.query(processedApiSql.sql(), connection, processedApiSql.params());
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

            // 根据是否有分页插件决定返回格式
            if (hasPaginationPlugin) {
                // 执行count查询获取总记录数
                long total = countRows(apiSql, connection);
                PageInfo pageInfo = new PageInfo(
                    Integer.parseInt(requestParams.get("page") + ""),
                    list.size(),
                    Integer.parseInt(requestParams.get("pageSize") + ""),
                    total);
                Result<Object> success = Result.success(list);
                success.setPageInfo(pageInfo);
                return success;
            } else {
                return Result.success(list);
            }
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
        } catch (SQLException | APIException | JSQLParserException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail("解析SQL字段失败：" + e.getMessage());
        }
    }

    /**
     * 将查询的sql的where条件变成 where 1 = 0。不用查出数据
     *
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    private String wrapSqlForMetadata(String sql) throws JSQLParserException {
        sql = sql.replaceAll("#\\{[^}]*}", "?");
        PlainSelect select = (PlainSelect) CCJSqlParserUtil.parse(sql);
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT");
        sj.add(select.getSelectItems().stream().map(SelectItem::toString).collect(Collectors.joining(",")));
        sj.add("FROM");
        sj.add(select.getFromItem().toString());
        if (select.getJoins() != null) {
            sj.add(select.getJoins().stream().map(Join::toString).collect(Collectors.joining(" ")));
        }
        sj.add("WHERE 1 = 0");
        return sj.toString();
    }

}
