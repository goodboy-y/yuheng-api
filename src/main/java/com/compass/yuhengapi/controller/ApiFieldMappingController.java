package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import com.compass.yuhengapi.repo.ApiFieldMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/apiConfig/field-mapping")
public class ApiFieldMappingController {

    private final ApiFieldMappingRepository fieldMappingRepository;

    /**
     * 获取API的字段映射配置
     */
    @GetMapping("/{apiConfigId}")
    public Result<List<ApiFieldMapping>> getFieldMappings(@PathVariable("apiConfigId") String apiConfigId) {
        List<ApiFieldMapping> mappings = fieldMappingRepository.findByApiConfigId(apiConfigId);
        return Result.success(mappings);
    }

    /**
     * 保存API的字段映射配置
     */
    @PostMapping("/{apiConfigId}")
    public Result<String> saveFieldMappings(@PathVariable("apiConfigId") String apiConfigId,
                                            @RequestBody List<ApiFieldMapping> mappings) {
        // 先删除旧的映射配置
        fieldMappingRepository.deleteByApiConfigId(apiConfigId);

        // 保存新的映射配置
        for (ApiFieldMapping mapping : mappings) {
            mapping.setApiConfigId(apiConfigId);
            fieldMappingRepository.save(mapping);
        }

        return Result.success("保存成功");
    }

}
