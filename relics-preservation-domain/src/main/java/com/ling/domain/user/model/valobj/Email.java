package com.ling.domain.user.model.valobj;

import lombok.Value;
import java.util.regex.Pattern;

/**
 * 邮箱值对象
 * @Author: LingRJ
 * @Description: 封装邮箱的业务规则和验证逻辑
 * @DateTime: 2025/7/11
 */
@Value
public class Email {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    String value;
    
    private Email(String value) {
        this.value = value;
    }
    
    /**
     * 创建邮箱值对象
     * @param email 邮箱字符串
     * @return 邮箱值对象
     * @throws IllegalArgumentException 如果邮箱格式不正确
     */
    public static Email of(String email) {
        validateEmail(email);
        return new Email(email.toLowerCase().trim());
    }
    
    /**
     * 验证邮箱格式
     * @param email 邮箱
     */
    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        
        String trimmedEmail = email.trim();

        if (!trimmedEmail.contains("@")) {
            throw new IllegalArgumentException("邮箱必须包含@符号");
        }

        if (trimmedEmail.length() > 254) {
            throw new IllegalArgumentException("邮箱长度不能超过254个字符");
        }
        
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }
    
    /**
     * 检查邮箱是否有效
     * @param email 邮箱字符串
     * @return 是否有效
     */
    public static boolean isValid(String email) {
        try {
            validateEmail(email);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取邮箱域名
     * @return 域名
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : "";
    }
    
    /**
     * 获取邮箱用户名部分
     * @return 用户名部分
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(0, atIndex) : value;
    }
    
    /**
     * 检查是否为指定域名的邮箱
     * @param domain 域名
     * @return 是否匹配
     */
    public boolean isDomainOf(String domain) {
        return getDomain().equalsIgnoreCase(domain);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
