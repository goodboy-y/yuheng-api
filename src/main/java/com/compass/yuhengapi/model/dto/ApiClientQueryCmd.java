package com.compass.yuhengapi.model.dto;

import com.compass.yuhengapi.common.lang.QueryCmd;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiClientQueryCmd extends QueryCmd {

    private String name;

    private String clientId;


}
