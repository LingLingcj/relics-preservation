package com.ling.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 用户个人信息修改DTO
 * @DateTime: 2025/6/27 17:00
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateDTO {

    private String nickname;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String avatarUrl;

    private String title;
}