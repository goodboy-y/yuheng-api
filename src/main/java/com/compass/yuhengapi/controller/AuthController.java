package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ChangePasswordDto;
import com.compass.yuhengapi.model.dto.LoginDto;
import com.compass.yuhengapi.service.AuthService;
import com.compass.yuhengapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDto login) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", accessToken);
            tokens.put("refreshToken", refreshToken);
            return Result.success(tokens);
        } catch (Exception e) {
            return Result.fail("用户名密码错误");
        }
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.fail("refreshToken不能为空");
        }

        try {
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            if (username == null) {
                return Result.fail("无效的refreshToken");
            }

            if (jwtUtil.isTokenExpired(refreshToken)) {
                return Result.fail("refreshToken已过期");
            }

            String newAccessToken = jwtUtil.generateToken(username);
            String newRefreshToken = jwtUtil.generateRefreshToken(username);

            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);
            return Result.success(tokens);
        } catch (Exception e) {
            log.error("刷新token失败: {}", e.getMessage());
            return Result.fail("刷新token失败");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        return Result.success(null);
    }

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody ChangePasswordDto changePassword) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.changePassword(username, changePassword);
        return Result.success(null);
    }

}