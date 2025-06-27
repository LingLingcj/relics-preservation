package com.ling.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: LingRJ
 * @Description: 角色枚举
 * @DateTime: 2025/6/27 14:32
 **/
@AllArgsConstructor
@Getter
public enum RoleEnum {
    USER("USER","普通用户"),
    EXPERT("USER","专家")
    ;
    private final String role;
    private final String description;
}
