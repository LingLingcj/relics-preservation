package com.ling.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 用户消息请求响应DTO
 * @DateTime: 2025/6/30 22:28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    // 用户唯一标识
    private String username;
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
    // 角色
    private String role;
    // 头衔/职位
    private String title;
}
