package com.ling.domain.login.model.entity;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String userId;
    private String username;
    private String password;
    private String email;
    private String role;
    private Integer status; // 0: 禁用, 1: 启用
    private String createTime;
    private String updateTime;

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
