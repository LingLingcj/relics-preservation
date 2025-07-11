package com.ling.domain.user.event.handler;

import com.ling.domain.user.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户事件处理器
 * @Author: LingRJ
 * @Description: 处理用户领域事件，实现事件驱动的业务逻辑
 * @DateTime: 2025/7/11
 */
@Component
@Slf4j
public class UserEventHandler {
    
    /**
     * 处理用户注册事件
     * @param event 用户注册事件
     */
    @EventListener
    @Async
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("处理用户注册事件: 用户名={}, 角色={}, 邮箱={}, 时间={}", 
                event.getUsername(), event.getRole(), event.getEmail(), event.getOccurredOn());
        
        try {
            // 1. 发送欢迎邮件
            sendWelcomeEmail(event.getUsername(), event.getEmail());
            
            // 2. 初始化用户默认设置
            initializeUserDefaults(event.getUsername());
            
            // 3. 记录审计日志
            recordAuditLog("USER_REGISTERED", event.getUsername(), 
                    String.format("用户注册成功，角色: %s", event.getRole()));
            
            // 4. 通知其他系统
            notifyExternalSystems(event);
            
            log.info("用户注册事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理用户注册事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户登录事件
     * @param event 用户登录事件
     */
    @EventListener
    @Async
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        log.info("处理用户登录事件: 用户名={}, 角色={}, IP={}, 时间={}", 
                event.getUsername(), event.getRole(), event.getLoginIp(), event.getOccurredOn());
        
        try {
            // 1. 更新最后登录时间
            updateLastLoginTime(event.getUsername(), event.getOccurredOn());
            
            // 2. 记录登录日志
            recordLoginLog(event.getUsername(), event.getLoginIp(), event.getOccurredOn());
            
            // 3. 检查安全策略
            checkSecurityPolicy(event.getUsername(), event.getLoginIp());
            
            // 4. 更新用户活跃度统计
            updateUserActivityStats(event.getUsername());
            
            log.debug("用户登录事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理用户登录事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户资料更新事件
     * @param event 用户资料更新事件
     */
    @EventListener
    @Async
    public void handleUserProfileUpdated(UserProfileUpdatedEvent event) {
        log.info("处理用户资料更新事件: 用户名={}, 更新字段={}, 时间={}", 
                event.getUsername(), event.getUpdatedFields().keySet(), event.getOccurredOn());
        
        try {
            // 1. 记录资料变更日志
            recordProfileChangeLog(event.getUsername(), event.getUpdatedFields());
            
            // 2. 如果邮箱发生变更，发送确认邮件
            if (event.isFieldUpdated("email")) {
                sendEmailVerification(event.getUsername(), 
                        (String) event.getUpdatedValue("email"));
            }
            
            // 3. 同步到其他系统
            syncProfileToExternalSystems(event.getUsername(), event.getUpdatedFields());
            
            // 4. 更新缓存
            updateUserCache(event.getUsername());
            
            log.debug("用户资料更新事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理用户资料更新事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 处理密码修改事件
     * @param event 密码修改事件
     */
    @EventListener
    @Async
    public void handlePasswordChanged(PasswordChangedEvent event) {
        log.info("处理密码修改事件: 用户名={}, 修改原因={}, 时间={}", 
                event.getUsername(), event.getChangeReason(), event.getOccurredOn());
        
        try {
            // 1. 记录安全日志
            recordSecurityLog("PASSWORD_CHANGED", event.getUsername(), 
                    String.format("密码修改，原因: %s", event.getChangeReason()));
            
            // 2. 发送安全通知邮件
            sendPasswordChangeNotification(event.getUsername());
            
            // 3. 强制其他会话下线（可选）
            invalidateOtherSessions(event.getUsername());
            
            // 4. 更新密码策略统计
            updatePasswordPolicyStats(event.getUsername());
            
            log.debug("密码修改事件处理完成: {}", event.getUsername());
            
        } catch (Exception e) {
            log.error("处理密码修改事件失败: {} - {}", event.getUsername(), e.getMessage(), e);
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    private void sendWelcomeEmail(String username, String email) {
        if (email != null && !email.trim().isEmpty()) {
            log.info("发送欢迎邮件到: {} (用户: {})", email, username);
            // TODO: 实现邮件发送逻辑
        }
    }
    
    private void initializeUserDefaults(String username) {
        log.debug("初始化用户默认设置: {}", username);
        // TODO: 实现用户默认设置初始化
    }
    
    private void recordAuditLog(String action, String username, String details) {
        log.info("审计日志: 动作={}, 用户={}, 详情={}", action, username, details);
        // TODO: 实现审计日志记录
    }
    
    private void notifyExternalSystems(UserRegisteredEvent event) {
        log.debug("通知外部系统用户注册: {}", event.getUsername());
        // TODO: 实现外部系统通知
    }
    
    private void updateLastLoginTime(String username, java.time.LocalDateTime loginTime) {
        log.debug("更新最后登录时间: {} - {}", username, loginTime);
        // TODO: 实现最后登录时间更新
    }
    
    private void recordLoginLog(String username, String loginIp, java.time.LocalDateTime loginTime) {
        log.info("登录日志: 用户={}, IP={}, 时间={}", username, loginIp, loginTime);
        // TODO: 实现登录日志记录
    }
    
    private void checkSecurityPolicy(String username, String loginIp) {
        log.debug("检查安全策略: 用户={}, IP={}", username, loginIp);
        // TODO: 实现安全策略检查
    }
    
    private void updateUserActivityStats(String username) {
        log.debug("更新用户活跃度统计: {}", username);
        // TODO: 实现用户活跃度统计更新
    }
    
    private void recordProfileChangeLog(String username, java.util.Map<String, Object> changes) {
        log.info("资料变更日志: 用户={}, 变更={}", username, changes);
        // TODO: 实现资料变更日志记录
    }
    
    private void sendEmailVerification(String username, String newEmail) {
        log.info("发送邮箱验证邮件: 用户={}, 新邮箱={}", username, newEmail);
        // TODO: 实现邮箱验证邮件发送
    }
    
    private void syncProfileToExternalSystems(String username, java.util.Map<String, Object> changes) {
        log.debug("同步资料到外部系统: 用户={}, 变更={}", username, changes.keySet());
        // TODO: 实现外部系统同步
    }
    
    private void updateUserCache(String username) {
        log.debug("更新用户缓存: {}", username);
        // TODO: 实现用户缓存更新
    }
    
    private void recordSecurityLog(String action, String username, String details) {
        log.info("安全日志: 动作={}, 用户={}, 详情={}", action, username, details);
        // TODO: 实现安全日志记录
    }
    
    private void sendPasswordChangeNotification(String username) {
        log.info("发送密码修改通知: {}", username);
        // TODO: 实现密码修改通知邮件发送
    }
    
    private void invalidateOtherSessions(String username) {
        log.debug("强制其他会话下线: {}", username);
        // TODO: 实现会话管理
    }
    
    private void updatePasswordPolicyStats(String username) {
        log.debug("更新密码策略统计: {}", username);
        // TODO: 实现密码策略统计更新
    }
}
