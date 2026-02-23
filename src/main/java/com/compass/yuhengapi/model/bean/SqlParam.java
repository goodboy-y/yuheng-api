package com.compass.yuhengapi.model.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SqlParam {

    private String sql;

    private List<ApiParam> params;

}
