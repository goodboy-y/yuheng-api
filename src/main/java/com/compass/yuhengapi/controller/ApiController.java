package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.compass.yuhengapi.common.util.ExcelUtils;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.model.entities.ApiDatasource;
import com.compass.yuhengapi.model.entities.ApiFieldMapping;
import com.compass.yuhengapi.repo.ApiFieldMappingRepository;
import com.compass.yuhengapi.service.ApiConfigService;
import com.compass.yuhengapi.service.ApiDataSourceService;
import com.compass.yuhengapi.service.ApiService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ApiConfigService apiConfigService;

    @Autowired
    private ApiDataSourceService dbDataSourceService;

    @Autowired
    private ApiService apiService;

    @Autowired
    private ApiFieldMappingRepository fieldMappingRepository;

    @RequestMapping("/{path}")
    @RateLimiter(name = "apiRateLimiter", fallbackMethod = "rateLimitFallback")
    @CircuitBreaker(name = "apiCircuitBreaker", fallbackMethod = "circuitBreakerFallback")
    public Result<Object> api(@PathVariable("path") String path, HttpServletRequest request) {
        try {
            ApiConfig config = apiConfigService.getConfig(path);
            if (config == null) {
                return Result.fail("该接口不存在！！");
            }
            ApiDatasource datasource = dbDataSourceService.detail(config.getDatasource().getId());
            if (datasource == null) {
                return Result.fail("数据源不存在！！");
            }
            return apiService.executeSql(request, config, datasource);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }

    public Result<Object> rateLimitFallback(String path, HttpServletRequest request, Throwable throwable) {
        log.warn("API请求被限流: {}", path);
        return Result.fail("请求过于频繁，请稍后再试").setCode(ReturnCodeEnum.RC201.getCode());
    }

    public Result<Object> circuitBreakerFallback(String path, HttpServletRequest request, Throwable throwable) {
        log.warn("API请求触发熔断: {}", path);
        return Result.fail("服务暂时不可用，请稍后再试").setCode(ReturnCodeEnum.RC201.getCode());
    }

    /**
     * 导出数据接口
     */
    @RequestMapping("/export/{path}")
    public ResponseEntity<byte[]> exportData(@PathVariable("path") String path,
                                             HttpServletRequest request, HttpServletResponse response) {
        try {
            ApiConfig config = apiConfigService.getConfig(path);
            if (config == null) {
                throw new RuntimeException("该接口不存在！！");
            }
            ApiDatasource datasource = dbDataSourceService.detail(config.getDatasource().getId());
            if (datasource == null) {
                throw new RuntimeException("数据源不存在！！");
            }

            Result<Object> result = apiService.executeSql(request, config, datasource);

            // 处理Excel导出
            if (result.getData() instanceof List) {
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.getData();

                // 获取字段映射配置
                List<ApiFieldMapping> fieldMappings = fieldMappingRepository.findByApiConfigId(config.getId());
                Map<String, String> headerMapping = null;
                if (fieldMappings != null && !fieldMappings.isEmpty()) {
                    headerMapping = new HashMap<>();
                    for (ApiFieldMapping mapping : fieldMappings) {
                        headerMapping.put(mapping.getFieldName(), mapping.getDisplayName());
                    }
                }

                byte[] excelBytes = ExcelUtils.exportExcelWithHeaders("API数据导出", dataList, headerMapping);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

                String fileName = config.getName() + "_" + System.currentTimeMillis() + ".xlsx";
                String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
                headers.setContentDispositionFormData("attachment",encodedFileName );
                return ResponseEntity.ok().headers(headers).body(excelBytes);
            } else {
                throw new RuntimeException("没有可导出的数据");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 如果导出失败，返回错误信息
            return ResponseEntity.status(500)
                .contentType(MediaType.TEXT_PLAIN)
                .body(e.getMessage().getBytes());
        }

    }
    


}
