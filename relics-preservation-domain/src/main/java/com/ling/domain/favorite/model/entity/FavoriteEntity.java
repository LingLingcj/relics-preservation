package com.ling.domain.favorite.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteEntity {
    private Long id;
    // 文物ID
    private Long relicsId;
    // 用户名
    private String username;
    // 创建时间
    private LocalDateTime createTime;
    // 操作结果
    private boolean success;
    // 结果消息
    private String message;
} 