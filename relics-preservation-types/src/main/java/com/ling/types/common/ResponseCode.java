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
    INVALID_ROLE("1010", "无效的用户角色"),
    USER_NOT_EXIST("1011", "用户不存在"),
    PROFILE_UPDATE_FAILED("1012", "更新用户信息失败"),
    EMAIL_EXISTS("1013", "邮箱已存在"),
    PHONE_EXISTS("1014", "手机号已存在"),
    WRONG_ROLE("1015", "身份错误"),

    // 文物相关错误码
    RELICS_NOT_FOUND("2001", "未找到指定文物"),

    // 评论相关错误码
    FORBIDDEN("2101", "没有权限"),

    // 通用错误码
    INVALID_PARAM("3001", "参数错误"),
    UN_ERROR("9998", "未知错误")

    ;


    private final String code;
    private final String info;
} 