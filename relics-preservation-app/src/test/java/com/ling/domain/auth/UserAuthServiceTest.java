package com.ling.domain.auth;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.auth.model.valobj.LoginVO;
import com.ling.domain.auth.model.valobj.RegisterVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserAuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: LingRJ
 * @Description: test
 * @DateTime: 2025/6/27 14:07
 **/
@SpringBootTest
@Slf4j
public class UserAuthServiceTest {
    @Resource
    private IUserAuthService userAuthService;

    @Test
    public void register_test() {
        RegisterVO registerVO = new RegisterVO();
        registerVO.setUsername("admin");
        registerVO.setPassword("admin123");
        registerVO.setConfirmPassword("admin123");
        registerVO.setRole("expert");

        UserInfoVO userInfoVO1 = userAuthService.register(registerVO);
        log.info("注册结果：userInfo:{}", JSON.toJSONString(userInfoVO1));

        registerVO.setUsername("admin");
        registerVO.setPassword("admin123");
        registerVO.setConfirmPassword("admin123");
        registerVO.setRole("expert");

        UserInfoVO userInfoVO2 = userAuthService.register(registerVO);
        log.info("注册结果：userInfo:{}", JSON.toJSONString(userInfoVO2));

    }

    @Test
    public void login_test() {
        LoginVO loginVO = new LoginVO();
        loginVO.setUsername("admin");
        loginVO.setPassword("admin123");
        UserInfoVO userInfoVO = userAuthService.login(loginVO);
        log.info("登录结果：userInfo:{}", JSON.toJSONString(userInfoVO));
    }
}
