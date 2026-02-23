package com.compass.yuhengapi.service.impl;

import com.compass.yuhengapi.common.lang.APIException;
import com.compass.yuhengapi.model.dto.LoginDto;
import com.compass.yuhengapi.model.entities.ApiAccount;
import com.compass.yuhengapi.repo.ApiAccountRepository;
import com.compass.yuhengapi.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private ApiAccountRepository apiAccountRepository;

    @Override
    public void login(LoginDto login) {
        ApiAccount apiAccount = apiAccountRepository.findByUsername(login.getUsername());
        if (apiAccount == null) {
            throw new APIException("用户名或密码错误");
        }
        if (!apiAccount.getPassword().equals(login.getPassword())) {
            throw new APIException("用户名或密码错误");
        }
    }


}
