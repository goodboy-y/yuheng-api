package com.compass.yuhengapi.service;

import com.compass.yuhengapi.model.dto.ChangePasswordDto;
import com.compass.yuhengapi.model.dto.LoginDto;

public interface AuthService {

    void login(LoginDto login);

    void changePassword(ChangePasswordDto changePassword);

}
