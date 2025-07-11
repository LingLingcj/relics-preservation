package com.ling.domain.user.model.entity;

import com.ling.domain.user.event.*;
import com.ling.domain.user.model.valobj.*;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户聚合根
 * @Author: LingRJ
 * @Description: 用户领域的聚合根，封装用户的完整业务逻辑
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
@Slf4j
public class User {
    
    private Username username;
    private String nickname;
    private String fullName;
    private Password password;
    private Email email;
    private PhoneNumber phoneNumber;
    private String avatarUrl;
    private String title;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    /**
     * 创建新用户
     * @param username 用户名
     * @param rawPassword 原始密码
     * @param role 用户角色
     * @return 用户实例
     */
    public static User create(String username, String rawPassword, String role) {
        return create(username, rawPassword, role, null);
    }
    
    /**
     * 创建新用户（带邮箱）
     * @param username 用户名
     * @param rawPassword 原始密码
     * @param role 用户角色
     * @param email 邮箱
     * @return 用户实例
     */
    public static User create(String username, String rawPassword, String role, String email) {
        LocalDateTime now = LocalDateTime.now();
        
        User user = User.builder()
                .username(Username.of(username))
                .password(Password.of(rawPassword))
                .role(UserRole.fromCode(role))
                .status(UserStatus.ENABLED)
                // 默认昵称为用户名
                .nickname(username)
                .createTime(now)
                .updateTime(now)
                .build();
        
        if (email != null && !email.trim().isEmpty()) {
            user.email = Email.of(email);
        }
        
        // 发布用户注册事件
        DomainEventPublisher.publish(new UserRegisteredEvent(
                username, role, email));
        
        log.info("创建新用户: {}, 角色: {}", username, role);
        return user;
    }
    
    /**
     * 用户认证
     * @param rawPassword 原始密码
     * @return 认证是否成功
     */
    public boolean authenticate(String rawPassword) {
        if (!status.canLogin()) {
            log.warn("用户 {} 尝试登录，但状态为: {}", username.getValue(), status.getName());
            return false;
        }
        
        boolean authenticated = password.matches(rawPassword);
        
        if (authenticated) {
            // 发布登录成功事件
            DomainEventPublisher.publish(new UserLoggedInEvent(
                    username.getValue(), role.getCode()));
            log.info("用户 {} 登录成功", username.getValue());
        } else {
            log.warn("用户 {} 登录失败：密码错误", username.getValue());
        }
        
        return authenticated;
    }
    
    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws IllegalArgumentException 如果旧密码错误或新密码无效
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (!password.matches(oldPassword)) {
            throw new IllegalArgumentException("原密码错误");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("新密码不能与原密码相同");
        }

        this.password = Password.of(newPassword);
        this.updateTime = LocalDateTime.now();

        // 发布密码修改事件
        DomainEventPublisher.publish(new PasswordChangedEvent(username.getValue()));

        log.info("用户 {} 修改密码成功", username.getValue());
    }

    /**
     * 更新用户资料
     * @param nickname 昵称
     * @param fullName 真实姓名
     * @param email 邮箱
     * @param phoneNumber 手机号
     * @param avatarUrl 头像URL
     * @param title 头衔
     */
    public void updateProfile(String nickname, String fullName, String email,
                            String phoneNumber, String avatarUrl, String title) {
        Map<String, Object> updatedFields = new HashMap<>();

        if (nickname != null && !nickname.trim().isEmpty() && !nickname.equals(this.nickname)) {
            this.nickname = nickname.trim();
            updatedFields.put("nickname", nickname);
        }

        if (fullName != null && !fullName.trim().isEmpty() && !fullName.equals(this.fullName)) {
            this.fullName = fullName.trim();
            updatedFields.put("fullName", fullName);
        }

        if (email != null && !email.trim().isEmpty()) {
            Email newEmail = Email.of(email);
            if (!newEmail.equals(this.email)) {
                this.email = newEmail;
                updatedFields.put("email", email);
            }
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            PhoneNumber newPhone = PhoneNumber.of(phoneNumber);
            if (!newPhone.equals(this.phoneNumber)) {
                this.phoneNumber = newPhone;
                updatedFields.put("phoneNumber", phoneNumber);
            }
        }

        if (avatarUrl != null && !avatarUrl.trim().isEmpty() && !avatarUrl.equals(this.avatarUrl)) {
            this.avatarUrl = avatarUrl.trim();
            updatedFields.put("avatarUrl", avatarUrl);
        }

        if (title != null && !title.trim().isEmpty() && !title.equals(this.title)) {
            this.title = title.trim();
            updatedFields.put("title", title);
        }

        if (!updatedFields.isEmpty()) {
            this.updateTime = LocalDateTime.now();

            // 发布用户资料更新事件
            DomainEventPublisher.publish(new UserProfileUpdatedEvent(
                    username.getValue(), updatedFields));

            log.info("用户 {} 更新资料: {}", username.getValue(), updatedFields.keySet());
        }
    }

    /**
     * 更改用户状态
     * @param newStatus 新状态
     * @param reason 状态变更原因
     * @throws IllegalArgumentException 如果状态转换不合法
     */
    public void changeStatus(UserStatus newStatus, String reason) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalArgumentException(
                    String.format("不能从状态 %s 转换到 %s", status.getName(), newStatus.getName()));
        }

        UserStatus oldStatus = this.status;
        this.status = newStatus;
        this.updateTime = LocalDateTime.now();

        log.info("用户 {} 状态从 {} 变更为 {}, 原因: {}",
                username.getValue(), oldStatus.getName(), newStatus.getName(), reason);
    }

    /**
     * 检查用户是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return status.isEnabled();
    }

    /**
     * 检查用户是否具有指定权限
     * @param permission 权限名称
     * @return 是否具有权限
     */
    public boolean hasPermission(String permission) {
        return role.hasPermission(permission);
    }

    /**
     * 检查用户是否为专家
     * @return 是否为专家
     */
    public boolean isExpert() {
        return role.isExpert();
    }

    /**
     * 检查密码是否需要升级
     * @return 是否需要升级
     */
    public boolean needsPasswordUpgrade() {
        return password.needsUpgrade();
    }

    /**
     * 获取用户显示名称
     * @return 显示名称（优先使用昵称，其次真实姓名，最后用户名）
     */
    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        return username.getValue();
    }
}
