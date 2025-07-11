package com.ling.domain.user.service;

import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.Username;

/**
 * 用户认证服务接口
 * @Author: LingRJ
 * @Description: 专门处理用户认证相关的领域服务
 * @DateTime: 2025/7/11
 */
public interface IUserAuthenticationService {
    
    /**
     * 用户登录认证
     * @param username 用户名
     * @param password 密码
     * @return 认证结果，包含用户信息
     */
    AuthenticationResult authenticate(String username, String password);
    
    /**
     * 用户登录认证（使用值对象）
     * @param username 用户名值对象
     * @param password 密码
     * @return 认证结果
     */
    AuthenticationResult authenticate(Username username, String password);
    
    /**
     * 检查用户是否存在
     * @param username 用户名
     * @return 是否存在
     */
    boolean userExists(String username);
    
    /**
     * 检查用户是否存在（使用值对象）
     * @param username 用户名值对象
     * @return 是否存在
     */
    boolean userExists(Username username);
    
    /**
     * 认证结果类
     */
    class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String failureReason;
        
        private AuthenticationResult(boolean success, String message, User user, String failureReason) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.failureReason = failureReason;
        }
        
        public static AuthenticationResult success(User user) {
            return new AuthenticationResult(true, "认证成功", user, null);
        }
        
        public static AuthenticationResult failure(String reason) {
            return new AuthenticationResult(false, "认证失败", null, reason);
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
        
        public String getFailureReason() {
            return failureReason;
        }
    }
}
