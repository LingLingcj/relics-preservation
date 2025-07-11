package com.ling.domain.user.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Arrays;

/**
 * 用户状态值对象
 * @Author: LingRJ
 * @Description: 封装用户状态的业务规则和状态转换逻辑
 * @DateTime: 2025/7/11
 */
@AllArgsConstructor
@Getter
public enum UserStatus {
    
    DISABLED((byte) 0, "禁用", "用户账户被禁用，无法登录和使用系统"),
    ENABLED((byte) 1, "启用", "用户账户正常，可以正常使用系统"),
    PENDING((byte) 2, "待审核", "用户注册后等待管理员审核"),
    LOCKED((byte) 3, "锁定", "用户账户因安全原因被临时锁定");
    
    private final byte code;
    private final String name;
    private final String description;
    
    /**
     * 根据代码获取用户状态
     * @param code 状态代码
     * @return 用户状态
     * @throws IllegalArgumentException 如果状态代码无效
     */
    public static UserStatus fromCode(byte code) {
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的用户状态代码: " + code));
    }
    
    /**
     * 根据代码获取用户状态（支持Byte类型）
     * @param code 状态代码
     * @return 用户状态
     */
    public static UserStatus fromCode(Byte code) {
        if (code == null) {
            throw new IllegalArgumentException("用户状态代码不能为空");
        }
        return fromCode(code.byteValue());
    }
    
    /**
     * 检查状态代码是否有效
     * @param code 状态代码
     * @return 是否有效
     */
    public static boolean isValidCode(byte code) {
        try {
            fromCode(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 检查用户是否可以登录
     * @return 是否可以登录
     */
    public boolean canLogin() {
        return this == ENABLED;
    }
    
    /**
     * 检查用户是否被禁用
     * @return 是否被禁用
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
    
    /**
     * 检查用户是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }
    
    /**
     * 检查用户是否待审核
     * @return 是否待审核
     */
    public boolean isPending() {
        return this == PENDING;
    }
    
    /**
     * 检查用户是否被锁定
     * @return 是否被锁定
     */
    public boolean isLocked() {
        return this == LOCKED;
    }
    
    /**
     * 检查是否可以转换到目标状态
     * @param targetStatus 目标状态
     * @return 是否可以转换
     */
    public boolean canTransitionTo(UserStatus targetStatus) {
        if (targetStatus == null) {
            return false;
        }
        
        // 定义状态转换规则
        switch (this) {
            case PENDING:
                return targetStatus == ENABLED || targetStatus == DISABLED;
            case ENABLED:
                return targetStatus == DISABLED || targetStatus == LOCKED;
            case DISABLED:
                return targetStatus == ENABLED;
            case LOCKED:
                return targetStatus == ENABLED || targetStatus == DISABLED;
            default:
                return false;
        }
    }
    
    /**
     * 获取状态转换的描述
     * @param targetStatus 目标状态
     * @return 转换描述
     */
    public String getTransitionDescription(UserStatus targetStatus) {
        if (!canTransitionTo(targetStatus)) {
            return "不允许的状态转换";
        }
        
        return String.format("从 %s 转换到 %s", this.name, targetStatus.name);
    }
    
    @Override
    public String toString() {
        return name;
    }
}
