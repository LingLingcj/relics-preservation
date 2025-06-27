package com.ling.domain.auth.service.impl;

import com.ling.domain.auth.adapter.IUserRepository;
import com.ling.domain.auth.model.entity.UserEntity;
import com.ling.domain.auth.model.valobj.ChangePasswordVO;
import com.ling.domain.auth.model.valobj.LoginVO;
import com.ling.domain.auth.model.valobj.RegisterVO;
import com.ling.domain.auth.model.valobj.UserInfoVO;
import com.ling.domain.auth.service.IUserAuthService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: LingRJ
 * @Description: 登录、注册实现
 * @DateTime: 2025/6/26 23:08
 **/
@Service
public class UserAuthServiceImpl implements IUserAuthService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserInfoVO register(RegisterVO registerVO) {
        // 密码确认校验
        if (!registerVO.getPassword().equals(registerVO.getConfirmPassword())) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.PASSWORD_CONFIRM_ERROR.getInfo())
                    .build();
        }
        
        // 用户名格式校验
        if (registerVO.getUsername() == null || 
            registerVO.getUsername().length() < 3 || 
            registerVO.getUsername().length() > 50) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.USERNAME_LENGTH_ERROR.getInfo())
                    .build();
        }
        
        if (!registerVO.getUsername().matches("^[a-z0-9_-]+$")) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.USERNAME_FORMAT_ERROR.getInfo())
                    .build();
        }
        
        // 用户名存在性校验
        if (userRepository.existsByUsername(registerVO.getUsername())) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.USERNAME_EXISTS.getInfo())
                    .build();
        }
        
        // 密码格式校验
        if (registerVO.getPassword() == null || 
            registerVO.getPassword().length() < 8 || 
            registerVO.getPassword().length() > 128) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.PASSWORD_LENGTH_ERROR.getInfo())
                    .build();
        }
        
        if (!registerVO.getPassword().matches("^[A-Za-z0-9!@#$%^&*]+$")) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.PASSWORD_FORMAT_ERROR.getInfo())
                    .build();
        }

        // 创建用户并加密密码
        UserEntity user = UserEntity.builder()
                .username(registerVO.getUsername())
                .password(passwordEncoder.encode(registerVO.getPassword()))
                .role(registerVO.getRole())
                .nickname(registerVO.getUsername())
                .status((byte) 1)
                .build();

        // 保存用户
        userRepository.save(user);

        return UserInfoVO.builder()
                .role(registerVO.getRole())
                .success(true)
                .username(user.getUsername())
                .message(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    @Override
    public UserInfoVO login(LoginVO loginVO) {
        // 格式校验
        if (loginVO.getUsername() == null || loginVO.getPassword() == null) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.LOGIN_ERROR.getInfo())
                    .build();
        }
        
        // 从仓库获取用户
        UserEntity userEntity = userRepository.findByUsername(loginVO.getUsername());
        if (userEntity == null) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.LOGIN_ERROR.getInfo())
                    .build();
        }
        
        // 验证密码
        if (!passwordEncoder.matches(loginVO.getPassword(), userEntity.getPassword())) {
            return UserInfoVO.builder()
                    .success(false)
                    .message(ResponseCode.LOGIN_ERROR.getInfo())
                    .build();
        }

        return UserInfoVO.builder()
                .success(true)
                .username(userEntity.getUsername())
                .role(userEntity.getRole())
                .message(ResponseCode.SUCCESS.getInfo())
                .build();
    }

    @Override
    @Transactional
    public boolean changePassword(ChangePasswordVO changePasswordVO, String username) {
        // 检查用户是否存在
        UserEntity userEntity = userRepository.findByUsername(username);
        if (userEntity == null) {
            return false;
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(changePasswordVO.getOldPassword(), userEntity.getPassword())) {
            return false;
        }
        
        // 校验新密码和确认密码
        if (!changePasswordVO.getNewPassword().equals(changePasswordVO.getConfirmPassword())) {
            return false;
        }
        
        // 校验新密码格式
        if (changePasswordVO.getNewPassword() == null || 
            changePasswordVO.getNewPassword().length() < 8 || 
            changePasswordVO.getNewPassword().length() > 128) {
            return false;
        }
        
        if (!changePasswordVO.getNewPassword().matches("^[A-Za-z0-9!@#$%^&*]+$")) {
            return false;
        }

        // 更新密码
        userEntity.setPassword(passwordEncoder.encode(changePasswordVO.getNewPassword()));
        userRepository.updatePassword(userEntity);
        
        return true;
    }

    @Override
    public boolean validateToken(String token) {
        // 实际的JWT验证在JwtAuthenticationFilter中处理，这里仅供业务逻辑扩展
        return true;
    }

    @Override
    public boolean isExist(String username) {
        return userRepository.existsByUsername(username);
    }
}
