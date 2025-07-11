package com.ling.domain.user.service.impl;

import com.ling.domain.user.adapter.IUserRepository;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.Username;
import com.ling.domain.user.service.IUserAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户认证服务实现
 * @Author: LingRJ
 * @Description: 实现用户认证相关的业务逻辑
 * @DateTime: 2025/7/11
 */
@Service
@Slf4j
public class UserAuthenticationServiceImpl implements IUserAuthenticationService {
    
    @Autowired
    private IUserRepository userRepository;
    
    @Override
    public AuthenticationResult authenticate(String username, String password) {
        try {
            return authenticate(Username.of(username), password);
        } catch (IllegalArgumentException e) {
            return AuthenticationResult.failure("用户名格式不正确: " + e.getMessage());
        }
    }
    
    @Override
    public AuthenticationResult authenticate(Username username, String password) {
        try {
            // 查找用户
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("用户认证失败: 用户不存在 - {}", username.getValue());
                return AuthenticationResult.failure("用户名或密码错误");
            }
            
            User user = userOpt.get();
            
            // 检查用户状态
            if (!user.isEnabled()) {
                log.warn("用户认证失败: 用户已禁用 - {}", username.getValue());
                return AuthenticationResult.failure("用户账户已被禁用");
            }
            
            // 验证密码
            boolean authenticated = user.authenticate(password);
            if (authenticated) {
                log.info("用户认证成功: {}", username.getValue());
                return AuthenticationResult.success(user);
            } else {
                log.warn("用户认证失败: 密码错误 - {}", username.getValue());
                return AuthenticationResult.failure("用户名或密码错误");
            }
            
        } catch (Exception e) {
            log.error("用户认证过程中发生异常: {} - {}", username.getValue(), e.getMessage(), e);
            return AuthenticationResult.failure("认证过程中发生错误");
        }
    }
    
    @Override
    public boolean userExists(String username) {
        try {
            return userExists(Username.of(username));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean userExists(Username username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (Exception e) {
            log.error("检查用户是否存在时发生异常: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }
}
