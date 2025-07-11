package com.ling.domain.user;

import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户领域测试
 * @Author: LingRJ
 * @Description: 测试用户领域的核心业务逻辑
 * @DateTime: 2025/7/11
 */
public class UserDomainTest {
    
    @Test
    @DisplayName("测试用户名值对象创建和验证")
    public void testUsernameValueObject() {
        // 测试有效用户名
        Username validUsername = Username.of("testuser123");
        assertEquals("testuser123", validUsername.getValue());
        
        // 测试无效用户名
        assertThrows(IllegalArgumentException.class, () -> Username.of(""));
        assertThrows(IllegalArgumentException.class, () -> Username.of("ab")); // 太短
        assertThrows(IllegalArgumentException.class, () -> Username.of("Test@User")); // 包含非法字符
        
        // 测试用户名验证方法
        assertTrue(Username.isValid("validuser"));
        assertFalse(Username.isValid("invalid@user"));
    }
    
    @Test
    @DisplayName("测试邮箱值对象创建和验证")
    public void testEmailValueObject() {
        // 测试有效邮箱
        Email validEmail = Email.of("test@example.com");
        assertEquals("test@example.com", validEmail.getValue());
        assertEquals("example.com", validEmail.getDomain());
        assertEquals("test", validEmail.getLocalPart());
        
        // 测试无效邮箱
        assertThrows(IllegalArgumentException.class, () -> Email.of(""));
        assertThrows(IllegalArgumentException.class, () -> Email.of("invalid-email"));
        assertThrows(IllegalArgumentException.class, () -> Email.of("test@"));
        
        // 测试邮箱验证方法
        assertTrue(Email.isValid("valid@email.com"));
        assertFalse(Email.isValid("invalid-email"));
    }
    
    @Test
    @DisplayName("测试密码值对象创建和验证")
    public void testPasswordValueObject() {
        // 测试有效密码
        Password validPassword = Password.of("password123");
        assertTrue(validPassword.matches("password123"));
        assertFalse(validPassword.matches("wrongpassword"));
        
        // 测试无效密码
        assertThrows(IllegalArgumentException.class, () -> Password.of(""));
        assertThrows(IllegalArgumentException.class, () -> Password.of("short")); // 太短
        assertThrows(IllegalArgumentException.class, () -> Password.of("onlyletters")); // 只有字母
        assertThrows(IllegalArgumentException.class, () -> Password.of("12345678")); // 只有数字
        
        // 测试密码验证方法
        assertTrue(Password.isValid("validpass123"));
        assertFalse(Password.isValid("invalid"));
    }
    
    @Test
    @DisplayName("测试手机号值对象创建和验证")
    public void testPhoneNumberValueObject() {
        // 测试有效手机号
        PhoneNumber validPhone = PhoneNumber.of("13812345678");
        assertEquals("13812345678", validPhone.getValue());
        assertEquals("138****5678", validPhone.getMasked());
        
        // 测试无效手机号
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.of(""));
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.of("12345678901")); // 不是1开头
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.of("1381234567")); // 位数不对
        
        // 测试手机号验证方法
        assertTrue(PhoneNumber.isValid("13812345678"));
        assertFalse(PhoneNumber.isValid("12345678901"));
    }
    
    @Test
    @DisplayName("测试用户角色值对象")
    public void testUserRoleValueObject() {
        // 测试角色创建
        UserRole userRole = UserRole.fromCode("USER");
        assertEquals("USER", userRole.getCode());
        assertEquals("普通用户", userRole.getDescription());
        assertTrue(userRole.hasPermission("READ_RELICS"));
        assertFalse(userRole.hasPermission("UPLOAD_RELICS"));
        
        UserRole expertRole = UserRole.fromCode("EXPERT");
        assertEquals("EXPERT", expertRole.getCode());
        assertTrue(expertRole.hasPermission("UPLOAD_RELICS"));
        assertTrue(expertRole.isHigherOrEqualTo(userRole));
        
        // 测试无效角色
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromCode("INVALID"));
        
        // 测试角色验证方法
        assertTrue(UserRole.isValidCode("USER"));
        assertTrue(UserRole.isValidCode("EXPERT"));
        assertFalse(UserRole.isValidCode("INVALID"));
    }
    
    @Test
    @DisplayName("测试用户状态值对象")
    public void testUserStatusValueObject() {
        // 测试状态创建
        UserStatus enabledStatus = UserStatus.fromCode((byte) 1);
        assertEquals("启用", enabledStatus.getName());
        assertTrue(enabledStatus.canLogin());
        assertTrue(enabledStatus.isEnabled());
        
        UserStatus disabledStatus = UserStatus.fromCode((byte) 0);
        assertEquals("禁用", disabledStatus.getName());
        assertFalse(disabledStatus.canLogin());
        assertTrue(disabledStatus.isDisabled());
        
        // 测试状态转换
        assertTrue(enabledStatus.canTransitionTo(disabledStatus));
        assertTrue(disabledStatus.canTransitionTo(enabledStatus));
        
        // 测试无效状态
        assertThrows(IllegalArgumentException.class, () -> UserStatus.fromCode((byte) 99));
    }
    
    @Test
    @DisplayName("测试用户聚合根创建")
    public void testUserAggregateCreation() {
        // 测试用户创建
        User user = User.create("testuser", "password123", "USER");
        
        assertNotNull(user);
        assertEquals("testuser", user.getUsername().getValue());
        assertEquals("USER", user.getRole().getCode());
        assertTrue(user.getStatus().isEnabled());
        assertEquals("testuser", user.getNickname()); // 默认昵称为用户名
        
        // 测试用户认证
        assertTrue(user.authenticate("password123"));
        assertFalse(user.authenticate("wrongpassword"));
    }
    
    @Test
    @DisplayName("测试用户密码修改")
    public void testUserPasswordChange() {
        User user = User.create("testuser", "oldpassword123", "USER");
        
        // 测试正确的密码修改
        assertDoesNotThrow(() -> user.changePassword("oldpassword123", "newpassword456"));
        assertTrue(user.authenticate("newpassword456"));
        assertFalse(user.authenticate("oldpassword123"));
        
        // 测试错误的旧密码
        assertThrows(IllegalArgumentException.class, 
                () -> user.changePassword("wrongpassword", "newpassword789"));
        
        // 测试相同的新密码
        assertThrows(IllegalArgumentException.class, 
                () -> user.changePassword("newpassword456", "newpassword456"));
    }
    
    @Test
    @DisplayName("测试用户资料更新")
    public void testUserProfileUpdate() {
        User user = User.create("testuser", "password123", "USER");
        
        // 测试资料更新
        assertDoesNotThrow(() -> user.updateProfile(
                "新昵称", 
                "真实姓名", 
                "test@example.com", 
                "13812345678", 
                "http://avatar.url", 
                "软件工程师"
        ));
        
        assertEquals("新昵称", user.getNickname());
        assertEquals("真实姓名", user.getFullName());
        assertEquals("test@example.com", user.getEmail().getValue());
        assertEquals("13812345678", user.getPhoneNumber().getValue());
        assertEquals("http://avatar.url", user.getAvatarUrl());
        assertEquals("软件工程师", user.getTitle());
    }
    
    @Test
    @DisplayName("测试用户状态变更")
    public void testUserStatusChange() {
        User user = User.create("testuser", "password123", "USER");
        
        // 测试状态变更
        assertDoesNotThrow(() -> user.changeStatus(UserStatus.DISABLED, "管理员禁用"));
        assertTrue(user.getStatus().isDisabled());
        assertFalse(user.isEnabled());
        
        // 测试恢复状态
        assertDoesNotThrow(() -> user.changeStatus(UserStatus.ENABLED, "管理员启用"));
        assertTrue(user.getStatus().isEnabled());
        assertTrue(user.isEnabled());
    }
    
    @Test
    @DisplayName("测试用户权限检查")
    public void testUserPermissions() {
        User normalUser = User.create("normaluser", "password123", "USER");
        User expertUser = User.create("expertuser", "password123", "EXPERT");
        
        // 测试普通用户权限
        assertTrue(normalUser.hasPermission("READ_RELICS"));
        assertTrue(normalUser.hasPermission("COMMENT"));
        assertFalse(normalUser.hasPermission("UPLOAD_RELICS"));
        assertFalse(normalUser.isExpert());
        
        // 测试专家用户权限
        assertTrue(expertUser.hasPermission("READ_RELICS"));
        assertTrue(expertUser.hasPermission("UPLOAD_RELICS"));
        assertTrue(expertUser.hasPermission("REVIEW_COMMENTS"));
        assertTrue(expertUser.isExpert());
    }
    
    @Test
    @DisplayName("测试用户显示名称")
    public void testUserDisplayName() {
        User user = User.create("testuser", "password123", "USER");
        
        // 默认显示名称为用户名
        assertEquals("testuser", user.getDisplayName());
        
        // 设置昵称后显示昵称
        user.updateProfile("我的昵称", null, null, null, null, null);
        assertEquals("我的昵称", user.getDisplayName());
        
        // 设置真实姓名，但有昵称时仍显示昵称
        user.updateProfile("我的昵称", "真实姓名", null, null, null, null);
        assertEquals("我的昵称", user.getDisplayName());
    }
}
