package com.compass.yuhengapi.filter;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONUtil;
import com.compass.yuhengapi.common.enumerate.ReturnCodeEnum;
import com.compass.yuhengapi.common.util.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class LoginFilter extends OncePerRequestFilter {

    protected final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/auth/login")
                || request.getServletPath().startsWith("/api")
                || request.getServletPath().startsWith("/assets")
                || request.getServletPath().equals("/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && stringRedisTemplate.hasKey(authorizationHeader.substring("Bearer ".length()))) {
            stringRedisTemplate.expire(authorizationHeader.substring("Bearer ".length()), 10 * 60 * 60L, TimeUnit.SECONDS);
            filterChain.doFilter(request, response);
        } else {
            try {
                // 设置响应编码和内容类型
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                允许跨域的相应头
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
                response.setHeader("Access-Control-Expose-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                // 写入响应
                JakartaServletUtil.write(response,
                        JSONUtil.toJsonStr(Result.custom(ReturnCodeEnum.RC401.getCode(), ReturnCodeEnum.RC401.getMessage(), null)),
                        MediaType.APPLICATION_JSON_VALUE
                );

            } catch (Exception e) {
                log.error("写入响应时发生未知异常: httpStatus={}, result={}", HttpStatus.OK.value(), "", e);
            }
        }

    }


}
