package com.ling.domain.user.service;

import com.ling.domain.user.model.entity.User;

/**
 * 用户注册服务接口
 * @Author: LingRJ
 * @Description: 专门处理用户注册相关的领域服务
 * @DateTime: 2025/7/11
 */
public interface IUserRegistrationService {
    
    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param role 角色
     * @return 注册结果
     */
    RegistrationResult register(String username, String password, String confirmPassword, String role);
    
    /**
     * 用户注册（带邮箱）
     * @param username 用户名
     * @param password 密码
     * @param confirmPassword 确认密码
     * @param role 角色
     * @param email 邮箱
     * @return 注册结果
     */
    RegistrationResult register(String username, String password, String confirmPassword, String role, String email);
    
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 是否可用
     */
    boolean isEmailAvailable(String email);
    
    /**
     * 注册结果类
     */
    class RegistrationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String errorCode;
        
        private RegistrationResult(boolean success, String message, User user, String errorCode) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.errorCode = errorCode;
        }
        
        public static RegistrationResult success(User user) {
            return new RegistrationResult(true, "注册成功", user, null);
        }
        
        public static RegistrationResult failure(String message, String errorCode) {
            return new RegistrationResult(false, message, null, errorCode);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public User getUser() {
            return user;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
    }
}
