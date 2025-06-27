package com.ling.domain.auth.service;

import com.ling.domain.auth.model.valobj.ProfileUpdateVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;

/**
 * @Author: LingRJ
 * @Description: 身份信息修改服务接口
 * @DateTime: 2025/6/27 16:00
 **/
public interface IUserProfileService {
    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoVO getUserInfo(String username);
    
    /**
     * 更新用户信息
     * @param profileUpdateVO 用户信息更新值对象
     * @param username 用户名
     * @return 更新后的用户信息
     */
    UserInfoVO updateUserProfile(ProfileUpdateVO profileUpdateVO, String username);
}
