package com.ling.domain.user.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色值对象
 * @Author: LingRJ
 * @Description: 封装用户角色的业务规则和权限逻辑
 * @DateTime: 2025/7/11
 */
@AllArgsConstructor
@Getter
public enum UserRole {
    
    USER("USER", "普通用户", 1, Set.of("READ_RELICS", "COMMENT", "FAVORITE")),
    EXPERT("EXPERT", "专家", 2, Set.of("READ_RELICS", "COMMENT", "FAVORITE", "UPLOAD_RELICS", "REVIEW_COMMENTS"));
    
    private final String code;
    private final String description;
    private final int level;
    private final Set<String> permissions;
    
    /**
     * 根据代码获取角色
     * @param code 角色代码
     * @return 用户角色
     * @throws IllegalArgumentException 如果角色代码无效
     */
    public static UserRole fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("角色代码不能为空");
        }
        
        return Arrays.stream(values())
                .filter(role -> role.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的角色代码: " + code));
    }
    
    /**
     * 检查角色代码是否有效
     * @param code 角色代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取所有可用的角色代码
     * @return 角色代码集合
     */
    public static Set<String> getAllCodes() {
        return Arrays.stream(values())
                .map(UserRole::getCode)
                .collect(Collectors.toSet());
    }
    
    /**
     * 检查是否具有指定权限
     * @param permission 权限名称
     * @return 是否具有权限
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    /**
     * 检查是否具有所有指定权限
     * @param requiredPermissions 所需权限集合
     * @return 是否具有所有权限
     */
    public boolean hasAllPermissions(Set<String> requiredPermissions) {
        return permissions.containsAll(requiredPermissions);
    }
    
    /**
     * 检查是否具有任一指定权限
     * @param requiredPermissions 所需权限集合
     * @return 是否具有任一权限
     */
    public boolean hasAnyPermission(Set<String> requiredPermissions) {
        return requiredPermissions.stream().anyMatch(permissions::contains);
    }
    
    /**
     * 检查角色等级是否高于或等于指定角色
     * @param other 其他角色
     * @return 是否高于或等于
     */
    public boolean isHigherOrEqualTo(UserRole other) {
        return this.level >= other.level;
    }
    
    /**
     * 检查角色等级是否低于指定角色
     * @param other 其他角色
     * @return 是否低于
     */
    public boolean isLowerThan(UserRole other) {
        return this.level < other.level;
    }
    
    /**
     * 检查是否为专家角色
     * @return 是否为专家
     */
    public boolean isExpert() {
        return this == EXPERT;
    }
    
    /**
     * 检查是否为普通用户角色
     * @return 是否为普通用户
     */
    public boolean isUser() {
        return this == USER;
    }
    
    @Override
    public String toString() {
        return code;
    }
}
