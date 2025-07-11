package com.ling.domain.user.model.valobj;

import lombok.Value;

/**
 * 用户名值对象
 * @Author: LingRJ
 * @Description: 封装用户名的业务规则和验证逻辑
 * @DateTime: 2025/7/11
 */
@Value
public class Username {
    
    String value;
    
    private Username(String value) {
        this.value = value;
    }
    
    /**
     * 创建用户名值对象
     * @param username 用户名字符串
     * @return 用户名值对象
     * @throws IllegalArgumentException 如果用户名不符合规则
     */
    public static Username of(String username) {
        validateUsername(username);
        return new Username(username);
    }
    
    /**
     * 验证用户名格式
     * @param username 用户名
     */
    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        String trimmedUsername = username.trim();
        
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 50) {
            throw new IllegalArgumentException("用户名长度应为3-50个字符");
        }
        
        if (!trimmedUsername.matches("^[a-z0-9_-]+$")) {
            throw new IllegalArgumentException("用户名只能包含小写字母，数字，下划线和连字符");
        }
    }
    
    /**
     * 检查用户名是否有效
     * @param username 用户名字符串
     * @return 是否有效
     */
    public static boolean isValid(String username) {
        try {
            validateUsername(username);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取用户名长度
     * @return 用户名长度
     */
    public int length() {
        return value.length();
    }
    
    /**
     * 检查是否包含指定字符
     * @param character 字符
     * @return 是否包含
     */
    public boolean contains(char character) {
        return value.indexOf(character) >= 0;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
