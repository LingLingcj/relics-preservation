package com.ling.api.dto;

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
    // 昵称
    private String nickname;
    // 真实姓名
    private String fullName;
    // 邮箱
    private String email;
    // 手机号码
    private String phoneNumber;
    // 头像URL
    private String avatarUrl;
    // 头衔/职位
    private String title;
} 