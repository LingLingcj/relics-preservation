package com.ling.domain.user.model.valobj;

import lombok.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码值对象
 * @Author: LingRJ
 * @Description: 封装密码的业务规则、加密和验证逻辑
 * @DateTime: 2025/7/11
 */
@Value
public class Password {
    
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    
    private final String encodedValue;
    
    private Password(String encodedValue) {
        this.encodedValue = encodedValue;
    }
    
    /**
     * 从原始密码创建密码值对象（会进行加密）
     * @param rawPassword 原始密码
     * @return 密码值对象
     * @throws IllegalArgumentException 如果密码不符合规则
     */
    public static Password of(String rawPassword) {
        validatePassword(rawPassword);
        String encoded = PASSWORD_ENCODER.encode(rawPassword);
        return new Password(encoded);
    }
    
    /**
     * 从已加密的密码创建密码值对象
     * @param encodedPassword 已加密的密码
     * @return 密码值对象
     */
    public static Password fromEncoded(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("加密密码不能为空");
        }
        return new Password(encodedPassword);
    }
    
    /**
     * 验证原始密码格式
     * @param rawPassword 原始密码
     */
    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        if (rawPassword.length() < 8 || rawPassword.length() > 128) {
            throw new IllegalArgumentException("密码长度应为8-128个字符");
        }
        
        if (!rawPassword.matches("^[A-Za-z0-9!@#$%^&*]+$")) {
            throw new IllegalArgumentException("密码只能包含字母、数字和特殊字符(!@#$%^&*)");
        }
        
        // 检查密码强度
        boolean hasLetter = rawPassword.matches(".*[A-Za-z].*");
        boolean hasDigit = rawPassword.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("密码必须包含至少一个字母和一个数字");
        }
    }
    
    /**
     * 检查原始密码是否有效
     * @param rawPassword 原始密码
     * @return 是否有效
     */
    public static boolean isValid(String rawPassword) {
        try {
            validatePassword(rawPassword);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 验证原始密码是否匹配
     * @param rawPassword 原始密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword) {
        if (rawPassword == null) {
            return false;
        }
        return PASSWORD_ENCODER.matches(rawPassword, encodedValue);
    }
    
    /**
     * 检查是否需要重新加密（用于密码策略升级）
     * @return 是否需要重新加密
     */
    public boolean needsUpgrade() {
        return PASSWORD_ENCODER.upgradeEncoding(encodedValue);
    }
    
    /**
     * 获取加密后的密码值
     * @return 加密后的密码
     */
    public String getEncodedValue() {
        return encodedValue;
    }
    
    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
