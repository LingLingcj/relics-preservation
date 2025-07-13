package com.ling.domain.interaction.security;

import com.ling.domain.user.model.entity.User;
import com.ling.domain.user.model.valobj.UserRole;
import com.ling.domain.user.service.IUserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 评论审核权限验证切面
 * @Author: LingRJ
 * @Description: 通过AOP实现评论审核权限的统一验证
 * @DateTime: 2025/7/11
 */
@Aspect
@Component
@Slf4j
public class CommentReviewPermissionAspect {
    
    @Autowired
    private IUserManagementService userManagementService;
    
    /**
     * 权限验证切面
     * @param joinPoint 连接点
     * @param permission 权限注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(permission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, CommentReviewPermission permission) throws Throwable {
        
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("用户未认证，拒绝访问: {}", joinPoint.getSignature().getName());
            throw new AccessDeniedException("用户未认证");
        }
        
        String username = authentication.getName();
        log.debug("检查用户 {} 的评论审核权限", username);
        
        try {
            // 获取用户信息
            Optional<User> userOpt = userManagementService.getUserInfo(username);
            if (userOpt.isEmpty()) {
                log.warn("用户不存在: {}", username);
                throw new AccessDeniedException("用户不存在");
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();
            
            // 检查权限
            boolean hasPermission = false;
            
            if (permission.allowExpert() && role.isExpert()) {
                hasPermission = true;
                log.debug("用户 {} 具有专家权限", username);
            }
            
            if (permission.allowAdmin() && role.isAdmin()) {
                hasPermission = true;
                log.debug("用户 {} 具有管理员权限", username);
            }
            
            if (!hasPermission) {
                log.warn("用户 {} (角色: {}) 没有评论审核权限", username, role.getCode());
                throw new AccessDeniedException("没有评论审核权限");
            }
            
            log.debug("用户 {} 权限验证通过，执行方法: {}", username, joinPoint.getSignature().getName());
            
            // 执行原方法
            return joinPoint.proceed();
            
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            log.error("权限验证过程中发生异常: {} - {}", username, e.getMessage(), e);
            throw new AccessDeniedException("权限验证失败");
        }
    }
    
    /**
     * 类级别权限验证
     * @param joinPoint 连接点
     * @param permission 权限注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@within(permission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, CommentReviewPermission permission) throws Throwable {
        return checkPermission(joinPoint, permission);
    }
}
