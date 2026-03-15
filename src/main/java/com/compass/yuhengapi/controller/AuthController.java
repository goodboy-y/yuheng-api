package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.LoginDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto login) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = generateToken(userDetails.getUsername());
            return Result.success(token);
        } catch (Exception e) {
            return Result.fail("用户名密码错误");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        // JWT是无状态的，退出登录只需客户端删除token即可
        // 这里可以添加额外的逻辑，比如将token加入黑名单等
        return Result.success(null);
    }

    private String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 60 * 1000)) // 10小时过期
            .signWith(SignatureAlgorithm.HS512, "your-secret-key") // 实际应用中应使用配置的密钥
            .compact();
    }


}
