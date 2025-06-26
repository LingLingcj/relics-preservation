package com.ling.domain.login.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String userId;
    // 用户密码
    private String password;
}
