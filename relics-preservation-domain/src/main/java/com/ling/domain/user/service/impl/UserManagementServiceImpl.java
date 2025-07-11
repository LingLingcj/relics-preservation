package com.ling.domain.user.service.impl;

import com.ling.domain.user.adapter.IUserRepository;
import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.*;
import com.ling.domain.user.service.IUserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 用户管理服务实现
 * @Author: LingRJ
 * @Description: 实现用户管理相关的业务逻辑
 * @DateTime: 2025/7/11
 */
@Service
@Slf4j
public class UserManagementServiceImpl implements IUserManagementService {
    
    @Autowired
    private IUserRepository userRepository;
    
    @Override
    public Optional<User> getUserInfo(String username) {
        try {
            return getUserInfo(Username.of(username));
        } catch (IllegalArgumentException e) {
            log.warn("获取用户信息失败: 用户名格式不正确 - {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> getUserInfo(Username username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("获取用户信息时发生异常: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    @Transactional
    public ProfileUpdateResult updateUserProfile(String username, String nickname, String fullName, 
                                               String email, String phoneNumber, String avatarUrl, String title) {
        try {
            // 查找用户
            Username usernameVO = Username.of(username);
            Optional<User> userOpt = userRepository.findByUsername(usernameVO);
            if (userOpt.isEmpty()) {
                return ProfileUpdateResult.failure("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 验证邮箱格式（如果提供）
            if (email != null && !email.trim().isEmpty() && !Email.isValid(email)) {
                return ProfileUpdateResult.failure("邮箱格式不正确");
            }
            
            // 验证手机号格式（如果提供）
            if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !PhoneNumber.isValid(phoneNumber)) {
                return ProfileUpdateResult.failure("手机号格式不正确");
            }
            
            // 检查邮箱是否已被其他用户使用
            if (email != null && !email.trim().isEmpty()) {
                Email emailVO = Email.of(email);
                if (userRepository.existsByEmailExcludeCurrentUser(emailVO, usernameVO)) {
                    return ProfileUpdateResult.failure("邮箱已被其他用户使用");
                }
            }
            
            // 检查手机号是否已被其他用户使用
            if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                PhoneNumber phoneVO = PhoneNumber.of(phoneNumber);
                if (userRepository.existsByPhoneNumberExcludeCurrentUser(phoneVO, usernameVO)) {
                    return ProfileUpdateResult.failure("手机号已被其他用户使用");
                }
            }
            
            // 更新用户资料
            user.updateProfile(nickname, fullName, email, phoneNumber, avatarUrl, title);
            
            // 保存更新
            boolean saveResult = userRepository.save(user);
            if (!saveResult) {
                return ProfileUpdateResult.failure("保存用户信息失败");
            }
            
            log.info("用户资料更新成功: {}", username);
            return ProfileUpdateResult.success(user);
            
        } catch (IllegalArgumentException e) {
            log.warn("更新用户资料失败: 参数验证错误 - {}", e.getMessage());
            return ProfileUpdateResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户资料时发生异常: {} - {}", username, e.getMessage(), e);
            return ProfileUpdateResult.failure("更新过程中发生错误");
        }
    }
    
    @Override
    @Transactional
    public PasswordChangeResult changePassword(String username, String oldPassword, String newPassword) {
        try {
            // 查找用户
            Username usernameVO = Username.of(username);
            Optional<User> userOpt = userRepository.findByUsername(usernameVO);
            if (userOpt.isEmpty()) {
                return PasswordChangeResult.failure("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 验证新密码格式
            if (!Password.isValid(newPassword)) {
                return PasswordChangeResult.failure("新密码格式不正确");
            }
            
            // 修改密码
            user.changePassword(oldPassword, newPassword);
            
            // 保存更新
            boolean saveResult = userRepository.save(user);
            if (!saveResult) {
                return PasswordChangeResult.failure("保存密码失败");
            }
            
            log.info("用户密码修改成功: {}", username);
            return PasswordChangeResult.success();
            
        } catch (IllegalArgumentException e) {
            log.warn("修改密码失败: {} - {}", username, e.getMessage());
            return PasswordChangeResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码时发生异常: {} - {}", username, e.getMessage(), e);
            return PasswordChangeResult.failure("修改密码过程中发生错误");
        }
    }
    
    @Override
    @Transactional
    public StatusChangeResult changeUserStatus(String username, UserStatus newStatus, String reason) {
        try {
            // 查找用户
            Username usernameVO = Username.of(username);
            Optional<User> userOpt = userRepository.findByUsername(usernameVO);
            if (userOpt.isEmpty()) {
                return StatusChangeResult.failure("用户不存在");
            }
            
            User user = userOpt.get();
            
            // 修改状态
            user.changeStatus(newStatus, reason);
            
            // 保存更新
            boolean saveResult = userRepository.save(user);
            if (!saveResult) {
                return StatusChangeResult.failure("保存用户状态失败");
            }
            
            log.info("用户状态修改成功: {} -> {}, 原因: {}", username, newStatus.getName(), reason);
            return StatusChangeResult.success(user);
            
        } catch (IllegalArgumentException e) {
            log.warn("修改用户状态失败: {} - {}", username, e.getMessage());
            return StatusChangeResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("修改用户状态时发生异常: {} - {}", username, e.getMessage(), e);
            return StatusChangeResult.failure("修改状态过程中发生错误");
        }
    }
}
