package com.compass.yuhengapi.model.dto;

import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiConfigWithMappingsDto {

    private ApiConfig apiConfig;
    private List<ApiFieldMapping> fieldMappings;

}
