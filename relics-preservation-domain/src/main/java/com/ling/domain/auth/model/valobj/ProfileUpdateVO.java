package com.ling.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 用户信息修改值对象
 * @DateTime: 2025/6/27 17:02
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateVO {
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