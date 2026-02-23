package com.compass.yuhengapi.model.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiClientVo {

    private String id;

    private String name;

    private String note;

    private String clientId;

    private String secret;

    private String expireDuration;

    private String expireAt;

    private String accountId;
}
