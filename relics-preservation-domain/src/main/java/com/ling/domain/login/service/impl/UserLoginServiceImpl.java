package com.ling.domain.login.service.impl;

import com.ling.domain.login.adapter.IUserRepository;
import com.ling.domain.login.model.valobj.LoginVO;
import com.ling.domain.login.model.valobj.RegisterVO;
import com.ling.domain.login.model.valobj.UserInfoVO;
import com.ling.domain.login.service.IUserLoginService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Author: LingRJ
 * @Description: 登录、注册实现
 * @DateTime: 2025/6/26 23:08
 **/
@Slf4j
@Service
public class UserLoginServiceImpl implements IUserLoginService {

    @Resource
    private IUserRepository repository;
    @Resource
    private PasswordEncoder passwordEncoder;


    @Override
    public UserInfoVO register(RegisterVO registerVO) {
        return null;
    }

    @Override
    public UserInfoVO login(LoginVO loginVO) {
        return null;
    }

    @Override
    public boolean validateToken(String token) {
        return false;
    }

    @Override
    public boolean isExist(String userId) {
        return false;
    }
}
