package com.compass.yuhengapi.controller;


import com.alibaba.fastjson2.JSON;
import com.compass.yuhengapi.common.util.ExcelUtils;
import com.compass.yuhengapi.common.util.PageList;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ApiConfigQueryCmd;
import com.compass.yuhengapi.model.dto.ApiConfigDetailDto;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import com.compass.yuhengapi.repo.ApiFieldMappingRepository;
import com.compass.yuhengapi.service.ApiConfigService;
import com.compass.yuhengapi.service.ApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/apiConfig")
public class ApiConfigController {

    private final ApiConfigService apiConfigService;
    private final ApiService apiService;
    private final ApiFieldMappingRepository fieldMappingRepository;

    @RequestMapping("/search")
    public Result<PageList<ApiConfig>> search(ApiConfigQueryCmd queryCmd) {
        return Result.success(apiConfigService.search(queryCmd));
    }

    /**
     * 新增API并保存字段映射和插件配置
     */
    @PostMapping("/add")
    public Result<String> addWithMappings(@RequestBody com.compass.yuhengapi.model.dto.ApiConfigWithMappingsDto dto) {
        String apiId = apiConfigService.addWithMappings(dto.getApiConfig(), dto.getFieldMappings(), dto.getPluginIds());
        return Result.success(apiId);
    }

    /**
     * 更新API并保存字段映射和插件配置
     */
    @PostMapping("/update")
    public Result<String> updateWithMappings(@RequestBody com.compass.yuhengapi.model.dto.ApiConfigWithMappingsDto dto) {
        apiConfigService.updateWithMappings(dto.getApiConfig(), dto.getFieldMappings(), dto.getPluginIds());
        return Result.success("修改成功");
    }

    @RequestMapping("/parse-sql-fields")
    public Result<List<String>> parseSqlFields(String datasourceId, String sql) {
        return apiService.parseSqlFields(datasourceId, sql);
    }

    @RequestMapping("/detail/{id}")
    public Result<ApiConfig> detail(@PathVariable("id") String id) {
        return Result.success(apiConfigService.detail(id));
    }

    /**
     * 获取API详情，包括字段映射和插件配置
     */
    @RequestMapping("/detail-full/{id}")
    public Result<ApiConfigDetailDto> detailFull(@PathVariable("id") String id) {
        return Result.success(apiConfigService.getApiConfigDetail(id));
    }

    @RequestMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") String id) {
        apiConfigService.delete(id);
        return Result.success("删除成功");
    }

    @RequestMapping("/unauthorized")
    public Result<PageList<ApiConfig>> getUnAuthorizedApiConfigs(@RequestParam String clientId, @RequestParam(defaultValue = "0") int pageNum, @RequestParam(defaultValue = "20") int pageSize, @RequestParam(required = false) String name) {
        return Result.success(apiConfigService.getUnAuthorizedApiConfigs(clientId, pageNum, pageSize, name));
    }

    /**
     * 测试API接口
     */
    @PostMapping("/test/{apiId}")
    public Result<Object> testApi(@PathVariable("apiId") String apiId, @RequestBody Map<String, Object> params) {
        try {
            return apiService.testApi(apiId, params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 导出API测试数据
     */
    @PostMapping("/export/{apiId}")
    public ResponseEntity<byte[]> exportTestData(@PathVariable("apiId") String apiId, @RequestBody Map<String, Object> params) {
        try {
            // 执行测试API获取数据
            Result<Object> result = apiService.testApi(apiId, params);

            // 获取API配置信息用于文件名
            ApiConfig config = apiConfigService.detail(apiId);
            if (config == null) {
                throw new RuntimeException("该接口不存在！！");
            }

            // 处理Excel导出
            if (result.getData() instanceof List) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.getData();
                if (CollectionUtils.isEmpty(dataList)) {
                    throw new RuntimeException("没有可导出的数据");
                }

                // 获取字段映射配置
                List<ApiFieldMapping> fieldMappings = fieldMappingRepository.findByApiConfigId(apiId);
                Map<String, String> headerMapping = null;
                Map<String, Integer> columnWidthMapping = null;
                if (fieldMappings != null && !fieldMappings.isEmpty()) {
                    headerMapping = new HashMap<>();
                    columnWidthMapping = new HashMap<>();
                    for (ApiFieldMapping mapping : fieldMappings) {
                        headerMapping.put(mapping.getFieldName(), mapping.getDisplayName());
                        if (mapping.getColumnWidth() != null) {
                            columnWidthMapping.put(mapping.getFieldName(), mapping.getColumnWidth());
                        }
                    }
                }

                byte[] excelBytes = ExcelUtils.exportExcelWithHeaders("Sheet1", dataList, headerMapping, columnWidthMapping);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
                // 允许前端访问Content-Disposition响应头
                headers.setAccessControlExposeHeaders(List.of("Content-Disposition"));

                String fileName = config.getName() + "_" + System.currentTimeMillis() + ".xlsx";
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
                headers.setContentDispositionFormData("attachment", encodedFileName);
                return ResponseEntity.ok().headers(headers).body(excelBytes);
            } else {
                throw new RuntimeException("没有可导出的数据");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 如果导出失败，返回错误信息
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(JSON.toJSONBytes(Result.fail(e.getMessage())));
        }
    }

}
