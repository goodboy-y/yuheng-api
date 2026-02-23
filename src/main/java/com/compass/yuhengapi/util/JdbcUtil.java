package com.compass.yuhengapi.util;

import com.compass.yuhengapi.model.entities.ApiDatasource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class JdbcUtil {

    @SuppressWarnings("all")
    public static ResultSet query(String sql, Connection connection, Object[] params) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        return preparedStatement.executeQuery();
    }

    public static Connection getConnection(ApiDatasource ds) throws SQLException, ClassNotFoundException {
        String url = ds.getUrl();
        switch (ds.getType()) {
            case DbType.MYSQL -> Class.forName("com.mysql.jdbc.Driver");
            case DbType.PG -> Class.forName("org.postgresql.Driver");
            case DbType.HIVE -> Class.forName("org.apache.hive.jdbc.HiveDriver");
            case DbType.SQLSERVER -> Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            case DbType.ORACLE -> Class.forName("oracle.jdbc.OracleDriver");
        }
        return DriverManager.getConnection(url, ds.getUsername(), ds.getPassword());
    }

}