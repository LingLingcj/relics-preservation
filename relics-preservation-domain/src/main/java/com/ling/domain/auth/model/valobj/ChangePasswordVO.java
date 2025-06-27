package com.ling.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 修改密码值对象
 * @DateTime: 2025/6/26 23:50
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordVO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
} 