package com.ling.domain.auth.service.impl;

import com.ling.domain.auth.adapter.IUserRepository;
import com.ling.domain.auth.model.entity.UserEntity;
import com.ling.domain.auth.model.valobj.ProfileUpdateVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserProfileService;
import com.ling.types.common.ResponseCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: LingRJ
 * @Description: 身份信息修改服务
 * @DateTime: 2025/6/27 16:00
 **/
@Service
public class UserProfileServiceImpl implements IUserProfileService {

    @Autowired
    private IUserRepository userRepository;

    @Override
    public UserInfoVO getUserInfo(String username) {
        try {
            UserEntity userEntity = getValidUserEntityOrThrow(username);
            // 构建并返回用户信息
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(userEntity, userInfoVO);
            userInfoVO.setSuccess(true);
            userInfoVO.setMessage(ResponseCode.SUCCESS.getInfo());
            return userInfoVO;

        } catch (IllegalArgumentException e) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public UserInfoVO updateUserProfile(ProfileUpdateVO profileUpdateVO, String username) {
        try {
            UserEntity userEntity = getValidUserEntityOrThrow(username);
            
            // 检查邮箱是否已存在
            if (profileUpdateVO.getEmail() != null && !profileUpdateVO.getEmail().isEmpty()) {
                if (userRepository.existsByEmailExcludeCurrentUser(profileUpdateVO.getEmail(), username)) {
                    return UserInfoVO.builder()
                            .success(false)
                            .message(ResponseCode.EMAIL_EXISTS.getInfo())
                            .build();
                }
            }
            
            // 检查手机号是否已存在
            if (profileUpdateVO.getPhoneNumber() != null && !profileUpdateVO.getPhoneNumber().isEmpty()) {
                if (userRepository.existsByPhoneNumberExcludeCurrentUser(profileUpdateVO.getPhoneNumber(), username)) {
                    return UserInfoVO.builder()
                            .success(false)
                            .message(ResponseCode.PHONE_EXISTS.getInfo())
                            .build();
                }
            }

            // 更新用户信息
            BeanUtils.copyProperties(profileUpdateVO, userEntity);

            // 保存更新
            boolean updateResult = userRepository.updateProfile(userEntity);
            if (!updateResult) {
                return UserInfoVO.builder()
                        .success(false)
                        .message("更新用户信息失败")
                        .build();
            }

            // 获取最新用户信息并返回
            UserEntity updatedUser = userRepository.findByUsername(username);
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtils.copyProperties(updatedUser, userInfoVO);
            userInfoVO.setSuccess(true);
            userInfoVO.setMessage(ResponseCode.SUCCESS.getInfo());
            return userInfoVO;
        } catch (IllegalArgumentException e) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    // 用户名验证
    private UserEntity getValidUserEntityOrThrow(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return userEntity;
    }
}
