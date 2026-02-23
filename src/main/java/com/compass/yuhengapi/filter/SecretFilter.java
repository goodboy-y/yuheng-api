package com.compass.yuhengapi.filter;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.entities.ApiClient;
import com.compass.yuhengapi.repo.ApiClientRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@SuppressWarnings("all")
public class SecretFilter extends OncePerRequestFilter {

    private final ApiClientRepository apiClientRepository;

    public SecretFilter(ApiClientRepository apiClientRepository) {
        this.apiClientRepository = apiClientRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String clientId = request.getHeader("clientId");
        String secret = request.getHeader("secret");
        List<ApiClient> apiClients = apiClientRepository.findByClientId(clientId);
        if (!apiClients.isEmpty()) {
            ApiClient apiClient = apiClients.get(0);
            if (apiClient.getSecret().equals(secret)) {
                // 符合条件放行
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
