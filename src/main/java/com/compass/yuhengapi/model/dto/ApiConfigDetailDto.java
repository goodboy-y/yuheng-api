package com.compass.yuhengapi.model.dto;

import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import com.compass.yuhengapi.model.entities.ApiPlugin;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiConfigDetailDto {

    private ApiConfig apiConfig;
    private List<ApiFieldMapping> fieldMappings;
    private List<ApiPlugin> plugins;

}