package com.compass.yuhengapi.model.bean;

/**
 * 解析后的的sql对象
 */
public record ApiSql(String sql, Object[] params) {

}
