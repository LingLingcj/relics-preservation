package com.ling.domain.auth.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 用户信息值对象
 * @DateTime: 2025/6/26 23:09
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    // 用户唯一标识
    private String username;
    // 昵称
    private String nickname;
    // 真实姓名
    private String fullName;
    // 密码 (应加密存储)
    private String password;
    // 邮箱
    private String email;
    // 手机号码
    private String phoneNumber;
    // 头像URL
    private String avatarUrl;
    // 用户状态 (1=正常, 0=禁用)
    private Byte status;
    // 角色
    private String role;
    // 头衔/职位
    private String title;
    // 权限级别或位掩码
    private Integer permission;
    // 操作成功标志
    private boolean success;
    // 响应消息
    private String message;
    // token
    private String token;
}
