package com.ling.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * @Author: LingRJ
 * @Description: 用户注册值对象
 * @DateTime: 2025/6/26 23:14
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterVO {
    // 用户Id
    private String username;
    // 用户密码
    private String password;
    // 确认密码
    private String confirmPassword;
    // 身份
    private String role;
    
    /**
     * 验证并获取角色枚举
     * @return 角色枚举值，如果不匹配则返回空
     */
    public Optional<RoleEnum> getRoleEnum() {
        return Arrays.stream(RoleEnum.values())
                .filter(roleEnum -> roleEnum.getRole().equalsIgnoreCase(this.role))
                .findFirst();
    }
    
    /**
     * 判断role是否为有效的枚举值
     * @return 如果是有效的角色枚举值返回true，否则返回false
     */
    public boolean isValidRole() {
        return getRoleEnum().isPresent();
    }
}
