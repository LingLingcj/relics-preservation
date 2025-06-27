package com.ling.types.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {
    SUCCESS("0000", "成功"),
    SYSTEM_ERROR("9999", "系统异常"),
    
    // 用户相关错误码
    USERNAME_LENGTH_ERROR("1001", "用户名长度应为3-50个字符"),
    USERNAME_FORMAT_ERROR("1002", "用户名只能包含小写字母，数字，下划线和连字符"),
    USERNAME_EXISTS("1003", "用户名已存在"),
    PASSWORD_LENGTH_ERROR("1004", "密码长度应为8-128个字符"),
    PASSWORD_FORMAT_ERROR("1005", "密码只能包含字母、数字和特殊字符"),
    PASSWORD_CONFIRM_ERROR("1006", "两次输入的密码不一致"),
    LOGIN_ERROR("1007", "用户名或密码错误"),
    OLD_PASSWORD_ERROR("1008", "原密码错误"),
    USER_NOT_LOGGED_IN("1009", "用户未登录"),
    INVALID_ROLE("1010", "无效的用户角色");

    private final String code;
    private final String info;
} 