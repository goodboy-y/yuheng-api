package com.compass.yuhengapi.service;

import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.model.dto.ApiClientQueryCmd;
import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.vo.ApiClientVo;

public interface ApiClientService {


    void add(ApiClient data);

    void update(ApiClient data);

    void delete(String id);

    ApiClientVo detail(String id);

    PageList<ApiClient> search(ApiClientQueryCmd queryCmd);

}
