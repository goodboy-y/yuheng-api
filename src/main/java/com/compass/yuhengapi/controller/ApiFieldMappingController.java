package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import com.compass.yuhengapi.repo.ApiFieldMappingRepository;
import jakarta.transaction.Transactional;
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
    @Transactional
    public Result<String> saveFieldMappings(@PathVariable("apiConfigId") String apiConfigId,
                                            @RequestBody List<ApiFieldMapping> mappings) {
        // 先删除旧的映射配置
        fieldMappingRepository.deleteByApiConfigId(apiConfigId);

        // 保存新的映射配置
        // 2. 将传入的 mappings 视为全新数据，重置ID和版本号
        for (ApiFieldMapping mapping : mappings) {
            mapping.setId(null); // 假设ID字段名为id
            mapping.setApiConfigId(apiConfigId);
        }

        // 3. 批量保存新的映射配置
        fieldMappingRepository.saveAll(mappings);
        return Result.success("保存成功");
    }

}
