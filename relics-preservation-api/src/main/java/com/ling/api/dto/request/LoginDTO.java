package com.ling.api.dto.request;

import com.ling.domain.user.model.valobj.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 登录DTO
 * @DateTime: 2025/6/27 14:39
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDTO {

    private UserRole role;

    private String username;

    private String password;
}
