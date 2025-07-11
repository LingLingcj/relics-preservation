package com.ling.domain.interaction.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 评论审核权限注解
 * @Author: LingRJ
 * @Description: 用于标记需要评论审核权限的方法
 * @DateTime: 2025/7/11
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommentReviewPermission {
    
    /**
     * 权限描述
     * @return 权限描述
     */
    String value() default "评论审核权限";
    
    /**
     * 是否允许专家角色
     * @return 是否允许专家
     */
    boolean allowExpert() default true;
    
    /**
     * 是否允许管理员角色
     * @return 是否允许管理员
     */
    boolean allowAdmin() default true;
}
