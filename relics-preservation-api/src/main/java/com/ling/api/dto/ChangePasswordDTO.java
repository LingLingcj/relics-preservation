package com.ling.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
