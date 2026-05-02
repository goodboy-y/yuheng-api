package com.compass.yuhengapi.controller;

import com.compass.yuhengapi.common.util.Result;
import com.compass.yuhengapi.model.dto.ChangePasswordDto;
import com.compass.yuhengapi.model.dto.LoginDto;
import com.compass.yuhengapi.service.AuthService;
import com.compass.yuhengapi.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final AuthService authService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto login) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            return Result.success(token);
        } catch (Exception e) {
            return Result.fail("用户名密码错误");
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