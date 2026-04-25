package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.common.lang.APIException;
import com.compass.yuhengapi.model.dto.ChangePasswordDto;
import com.compass.yuhengapi.model.dto.LoginDto;
import com.compass.yuhengapi.model.entities.ApiAccount;
import com.compass.yuhengapi.repo.ApiAccountRepository;
import com.compass.yuhengapi.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private ApiAccountRepository apiAccountRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public void login(LoginDto login) {
        ApiAccount apiAccount = apiAccountRepository.findByUsername(login.getUsername());
        if (apiAccount == null) {
            throw new APIException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(login.getPassword(), apiAccount.getPassword())) {
            throw new APIException("用户名或密码错误");
        }
    }

    @Override
    public void changePassword(ChangePasswordDto changePassword) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ApiAccount apiAccount = apiAccountRepository.findByUsername(username);
        if (apiAccount == null) {
            throw new APIException("用户不存在");
        }
        if (!passwordEncoder.matches(changePassword.getOldPassword(), apiAccount.getPassword())) {
            throw new APIException("原密码错误");
        }
        apiAccount.setPassword(passwordEncoder.encode(changePassword.getNewPassword()));
        apiAccountRepository.save(apiAccount);
    }


}
