package com.compass.yuhengapi.filter;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.model.entities.ApiConfig;
import com.compass.yuhengapi.repo.ApiClientRepository;
import com.compass.yuhengapi.repo.ApiConfigRepository;
import com.compass.yuhengapi.service.ApiAccessLogService;
import com.compass.yuhengapi.service.ApiConfigAccessService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("all")
public class ApiSecretFilter extends OncePerRequestFilter {

    private final ApiClientRepository apiClientRepository;
    private final ApiConfigRepository apiConfigRepository;
    private final ApiConfigAccessService apiConfigAccessService;
    @Autowired
    private ApiAccessLogService apiAccessLogService;

    public ApiSecretFilter(ApiClientRepository apiClientRepository,
                           ApiConfigRepository apiConfigRepository,
                           ApiConfigAccessService apiConfigAccessService) {
        this.apiClientRepository = apiClientRepository;
        this.apiConfigRepository = apiConfigRepository;
        this.apiConfigAccessService = apiConfigAccessService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!request.getServletPath().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = request.getHeader("clientId");
        String secret = request.getHeader("secret");
        List<ApiClient> apiClients = apiClientRepository.findByClientId(clientId);
        if (!apiClients.isEmpty()) {
            ApiClient apiClient = apiClients.get(0);
            if (apiClient.getSecret().equals(secret)) {
                // 验证客户端是否有权限访问该API,去掉"/api/"字符
                String apiPath = request.getServletPath().substring(5);
                ApiConfig apiConfig = apiConfigRepository.selectByPath(apiPath);

                if (apiConfig != null) {
                    boolean hasAccess = apiConfigAccessService.hasAccess(apiClient.getId(), apiConfig.getId());
                    if (!hasAccess) {
                        try {
                            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            JakartaServletUtil.write(response,
                                JSONUtil.toJsonStr(Result.custom(ReturnCodeEnum.ACCESS_DENIED.getCode(), "未授权访问此API", null)),
                                MediaType.APPLICATION_JSON_VALUE
                            );
                            return;
                        } catch (Exception e) {
                            log.error("写入响应时发生未知异常: httpStatus={}, result={}", HttpStatus.OK.value(), "", e);
                            return;
                        }
                    }
                }

                Map<String, String> params = new HashMap<>();
                Enumeration<String> paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String name = paramNames.nextElement();
                    params.put(name, request.getParameter(name));
                }
                apiAccessLogService.saveLog(apiPath, apiClient.getClientId(),
                    JSONUtil.toJsonStr(params), apiClient.getAccountId());

                filterChain.doFilter(request, response);
                return;
            }
        }
        try {
            // 设置响应编码和内容类型
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            // 写入响应
            JakartaServletUtil.write(response,
                JSONUtil.toJsonStr(Result.custom(ReturnCodeEnum.INVALID_TOKEN.getCode(), ReturnCodeEnum.INVALID_TOKEN.getMessage(), null)),
                MediaType.APPLICATION_JSON_VALUE
            );

        } catch (Exception e) {
            log.error("写入响应时发生未知异常: httpStatus={}, result={}", HttpStatus.OK.value(), "", e);
        }
    }


}
