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
import com.compass.yuhengapi.service.ApiService;
import com.compass.yuhengapi.util.JdbcUtil;
import com.compass.yuhengapi.util.PoolManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class ApiServiceImpl implements ApiService {

    @Override
    public Result<Object> executeSql(HttpServletRequest request, ApiConfig config, ApiDatasource datasource) {
        ApiSql apiSql = buildSql(request, config);
        if (apiSql == null) {
            return Result.custom(ReturnCodeEnum.RC400.getCode(), ReturnCodeEnum.RC400.getMessage(), null);
        }
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
        } catch (APIException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return Result.fail(e.getMessage());
        }
        return Result.fail();
    }

    private ApiSql buildSql(HttpServletRequest request, ApiConfig config) {
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

}
