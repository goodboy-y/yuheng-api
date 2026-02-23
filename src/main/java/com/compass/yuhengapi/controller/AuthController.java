package com.compass.yuhengapi.controller;

import cn.hutool.core.util.IdUtil;
import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.LoginDto;
import com.compass.yuhengapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    protected final StringRedisTemplate stringRedisTemplate;

    protected AuthService authService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto login) {

        try {
            authService.login(login);
            String data = IdUtil.fastSimpleUUID();
            stringRedisTemplate.opsForValue().set(data, "1", 60 * 10, TimeUnit.SECONDS);
            return Result.success(data);
        } catch (Exception e) {
            return Result.fail("用户名密码错误");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        stringRedisTemplate.delete(authorizationHeader.substring("Bearer ".length()));
        return Result.success(null);

    }


}
