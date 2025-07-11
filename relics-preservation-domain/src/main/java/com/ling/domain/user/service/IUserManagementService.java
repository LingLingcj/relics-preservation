package com.ling.domain.user.service;

import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.Username;
import com.ling.domain.user.model.valobj.UserStatus;

import java.util.Optional;

/**
 * 用户管理服务接口
 * @Author: LingRJ
 * @Description: 专门处理用户管理相关的领域服务
 * @DateTime: 2025/7/11
 */
public interface IUserManagementService {
    
    /**
     * 获取用户信息
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> getUserInfo(String username);
    
    /**
     * 获取用户信息（使用值对象）
     * @param username 用户名值对象
     * @return 用户信息
     */
    Optional<User> getUserInfo(Username username);
    
    /**
     * 更新用户资料
     * @param username 用户名
     * @param nickname 昵称
     * @param fullName 真实姓名
     * @param email 邮箱
     * @param phoneNumber 手机号
     * @param avatarUrl 头像URL
     * @param title 头衔
     * @return 更新结果
     */
    ProfileUpdateResult updateUserProfile(String username, String nickname, String fullName, 
                                        String email, String phoneNumber, String avatarUrl, String title);
    
    /**
     * 修改用户密码
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    PasswordChangeResult changePassword(String username, String oldPassword, String newPassword);
    
    /**
     * 修改用户状态
     * @param username 用户名
     * @param newStatus 新状态
     * @param reason 变更原因
     * @return 修改结果
     */
    StatusChangeResult changeUserStatus(String username, UserStatus newStatus, String reason);
    
    /**
     * 资料更新结果类
     */
    class ProfileUpdateResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        private ProfileUpdateResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public static ProfileUpdateResult success(User user) {
            return new ProfileUpdateResult(true, "资料更新成功", user);
        }
        
        public static ProfileUpdateResult failure(String message) {
            return new ProfileUpdateResult(false, message, null);
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
    }
    
    /**
     * 密码修改结果类
     */
    class PasswordChangeResult {
        private final boolean success;
        private final String message;
        
        private PasswordChangeResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static PasswordChangeResult success() {
            return new PasswordChangeResult(true, "密码修改成功");
        }
        
        public static PasswordChangeResult failure(String message) {
            return new PasswordChangeResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * 状态修改结果类
     */
    class StatusChangeResult {
        private final boolean success;
        private final String message;
        private final User user;
        
        private StatusChangeResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
        
        public static StatusChangeResult success(User user) {
            return new StatusChangeResult(true, "状态修改成功", user);
        }
        
        public static StatusChangeResult failure(String message) {
            return new StatusChangeResult(false, message, null);
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
    }
}
