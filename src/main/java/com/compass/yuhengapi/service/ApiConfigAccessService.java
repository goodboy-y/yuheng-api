package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiConfigAccessQueryCmd;
import com.compass.yuhengapi.model.entities.ApiConfigAccess;

public interface ApiConfigAccessService {

    void grant(String clientId, String apiConfigId);

    void revoke(String id);

    ApiConfigAccess detail(String id);

    PageList<ApiConfigAccess> search(ApiConfigAccessQueryCmd queryCmd);

    boolean hasAccess(String clientId, String apiConfigId);
}
