package com.ling.domain.login.model.valobj;

import lombok.*;

/**
 * @Author: LingRJ
 * @Description: 注册状态枚举
 * @DateTime: 2025/6/26 23:39
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RegisterStatusEnum {
    SUCCESS("注册成功"),
    USERNAME_EXISTS("用户名已存在"),
    EMAIL_EXISTS("邮箱已存在"),
    PHONE_EXISTS("手机号已存在")
    ;

    private String message;
}
