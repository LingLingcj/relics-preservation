package com.ling.domain.user.service.impl;

import com.ling.domain.user.adapter.IUserRepository;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.*;
import com.ling.domain.user.service.IUserRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户注册服务实现
 * @Author: LingRJ
 * @Description: 实现用户注册相关的业务逻辑
 * @DateTime: 2025/7/11
 */
@Service
@Slf4j
public class UserRegistrationServiceImpl implements IUserRegistrationService {
    
    @Autowired
    private IUserRepository userRepository;
    
    @Override
    @Transactional
    public RegistrationResult register(String username, String password, String confirmPassword, String role) {
        return register(username, password, confirmPassword, role, null);
    }
    
    @Override
    @Transactional
    public RegistrationResult register(String username, String password, String confirmPassword, String role, String email) {
        try {
            // 验证密码确认
            if (!password.equals(confirmPassword)) {
                return RegistrationResult.failure("两次输入的密码不一致", "PASSWORD_CONFIRM_ERROR");
            }
            
            // 验证用户名格式
            if (!Username.isValid(username)) {
                return RegistrationResult.failure("用户名格式不正确", "USERNAME_FORMAT_ERROR");
            }
            
            // 验证密码格式
            if (!Password.isValid(password)) {
                return RegistrationResult.failure("密码格式不正确", "PASSWORD_FORMAT_ERROR");
            }
            
            // 验证角色
            if (!UserRole.isValidCode(role)) {
                return RegistrationResult.failure("无效的用户角色", "INVALID_ROLE");
            }
            
            // 验证邮箱格式（如果提供）
            if (email != null && !email.trim().isEmpty() && !Email.isValid(email)) {
                return RegistrationResult.failure("邮箱格式不正确", "EMAIL_FORMAT_ERROR");
            }
            
            // 检查用户名是否已存在
            Username usernameVO = Username.of(username);
            if (userRepository.existsByUsername(usernameVO)) {
                return RegistrationResult.failure("用户名已存在", "USERNAME_EXISTS");
            }
            
            // 检查邮箱是否已存在（如果提供）
            if (email != null && !email.trim().isEmpty()) {
                Email emailVO = Email.of(email);
                if (userRepository.existsByEmail(emailVO)) {
                    return RegistrationResult.failure("邮箱已存在", "EMAIL_EXISTS");
                }
            }
            
            // 创建用户
            User user = User.create(username, password, role, email);
            
            // 保存用户
            boolean saveResult = userRepository.save(user);
            if (!saveResult) {
                return RegistrationResult.failure("保存用户信息失败", "SAVE_ERROR");
            }
            
            log.info("用户注册成功: {}", username);
            return RegistrationResult.success(user);
            
        } catch (IllegalArgumentException e) {
            log.warn("用户注册失败: 参数验证错误 - {}", e.getMessage());
            return RegistrationResult.failure(e.getMessage(), "VALIDATION_ERROR");
        } catch (Exception e) {
            log.error("用户注册过程中发生异常: {} - {}", username, e.getMessage(), e);
            return RegistrationResult.failure("注册过程中发生错误", "SYSTEM_ERROR");
        }
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        try {
            if (!Username.isValid(username)) {
                return false;
            }
            return !userRepository.existsByUsername(Username.of(username));
        } catch (Exception e) {
            log.error("检查用户名可用性时发生异常: {} - {}", username, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return true; // 空邮箱认为是可用的
            }
            if (!Email.isValid(email)) {
                return false;
            }
            return !userRepository.existsByEmail(Email.of(email));
        } catch (Exception e) {
            log.error("检查邮箱可用性时发生异常: {} - {}", email, e.getMessage(), e);
            return false;
        }
    }
}
