package com.compass.yuhengapi.model.dto;

import com.compass.yuhengapi.common.lang.QueryCmd;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiConfigAccessQueryCmd extends QueryCmd {

    private String clientId;

    private String apiConfigId;

    private String clientName;

    private String apiPath;
}
