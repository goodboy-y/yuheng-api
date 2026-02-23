package com.compass.yuhengapi.model.dto;

import com.compass.yuhengapi.common.lang.QueryCmd;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiDatasourceQueryCmd extends QueryCmd {

    private String name;

    private String type;


}
