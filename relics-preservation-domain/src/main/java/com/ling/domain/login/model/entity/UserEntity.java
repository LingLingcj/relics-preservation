package com.ling.domain.login.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {

    private String username;
    private String password;
    private String email;
    private String role;
    // 0: 禁用, 1: 启用
    private Byte status;
    private Date createTime;
    private Date updateTime;

    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
