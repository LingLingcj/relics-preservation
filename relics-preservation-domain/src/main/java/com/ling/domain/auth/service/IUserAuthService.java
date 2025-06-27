package com.ling.domain.auth.service;

import com.ling.domain.auth.model.valobj.ChangePasswordVO;
import com.ling.domain.auth.model.valobj.LoginVO;
import com.ling.domain.auth.model.valobj.RegisterVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;


/**
 * @Author: LingRJ
 * @Description: 用户登录、注册接口
 * @DateTime: 2025/6/26 23:07
 **/
public interface IUserAuthService {
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
     * 修改密码
     * @param changePasswordVO 修改密码值对象
     * @param username 当前用户名
     * @return 修改结果
     */
    boolean changePassword(ChangePasswordVO changePasswordVO, String username);


    /**
     * 验证用户是否存在
     * @param username 用户名
     * @return 验证结果
     */
    boolean isExist(String username);
}
