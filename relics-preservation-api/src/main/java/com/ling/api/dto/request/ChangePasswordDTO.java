package com.ling.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: ChangPasswordDTO
 * @DateTime: 2025/6/27 14:39
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordDTO {

    private String oldPassword;

    private String newPassword;

    private String confirmPassword;
}
