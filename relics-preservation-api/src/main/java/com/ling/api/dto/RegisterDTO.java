package com.ling.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ling.domain.auth.model.valobj.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: DTO
 * @DateTime: 2025/6/27 14:39
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDTO {
    // 用户Id
    private String username;

    // 用户密码
    private String password;

    // 确认密码
    private String confirmPassword;

    // 身份
    private RoleEnum role;
}
