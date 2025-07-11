package com.ling.domain.user.model.valobj;

import lombok.Value;
import java.util.regex.Pattern;

/**
 * 手机号值对象
 * @Author: LingRJ
 * @Description: 封装手机号的业务规则和验证逻辑
 * @DateTime: 2025/7/11
 */
@Value
public class PhoneNumber {
    
    // 中国大陆手机号正则表达式
    private static final Pattern CHINA_MOBILE_PATTERN = 
        Pattern.compile("^1[3-9]\\d{9}$");
    
    private final String value;
    
    private PhoneNumber(String value) {
        this.value = value;
    }
    
    /**
     * 创建手机号值对象
     * @param phoneNumber 手机号字符串
     * @return 手机号值对象
     * @throws IllegalArgumentException 如果手机号格式不正确
     */
    public static PhoneNumber of(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        return new PhoneNumber(phoneNumber.trim());
    }
    
    /**
     * 验证手机号格式
     * @param phoneNumber 手机号
     */
    private static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        
        String trimmedPhone = phoneNumber.trim();
        
        if (!CHINA_MOBILE_PATTERN.matcher(trimmedPhone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确，请输入有效的中国大陆手机号");
        }
    }
    
    /**
     * 检查手机号是否有效
     * @param phoneNumber 手机号字符串
     * @return 是否有效
     */
    public static boolean isValid(String phoneNumber) {
        try {
            validatePhoneNumber(phoneNumber);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取运营商类型
     * @return 运营商类型
     */
    public String getCarrierType() {
        if (value.startsWith("13") || value.startsWith("15") || value.startsWith("18")) {
            return "中国移动";
        } else if (value.startsWith("14") || value.startsWith("17") || value.startsWith("19")) {
            return "中国电信";
        } else if (value.startsWith("16")) {
            return "中国联通";
        }
        return "未知运营商";
    }
    
    /**
     * 获取脱敏的手机号（中间4位用*替代）
     * @return 脱敏手机号
     */
    public String getMasked() {
        if (value.length() == 11) {
            return value.substring(0, 3) + "****" + value.substring(7);
        }
        return value;
    }
    
    /**
     * 检查是否为指定运营商的手机号
     * @param carrier 运营商名称
     * @return 是否匹配
     */
    public boolean isCarrierOf(String carrier) {
        return getCarrierType().contains(carrier);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
