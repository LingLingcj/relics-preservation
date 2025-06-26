package com.ling.domain.login.service;

import com.ling.domain.login.model.valobj.LoginVO;
import com.ling.domain.login.model.valobj.RegisterVO;
import com.ling.domain.login.model.valobj.UserInfoVO;


/**
 * @Author: LingRJ
 * @Description: 用户登录、注册接口
 * @DateTime: 2025/6/26 23:07
 **/
public interface IUserLoginService {
    /**
     * 注册服务
     * @param registerVO 注册值对象
     * @return 用户信息
     */
    UserInfoVO register(RegisterVO registerVO);

    /**
     * 登录服务
     * @param loginVO 登录值对象
     * @return 用户信息
     */
    UserInfoVO login(LoginVO loginVO);

    /**
     * 验证token
     * @param token 用户token
     * @return 验证结果
     */
    boolean validateToken(String token);

    /**
     * 验证用户是否存在
     * @param userId 用户Id
     * @return 验证结果
     */
    boolean isExist(String userId);
}
