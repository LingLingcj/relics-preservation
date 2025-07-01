package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏表PO类
 * @author 31229
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Favorite {
    // 自增主键
    private Long id;
    // 文物ID
    private Long relicsId;
    // 用户名
    private String username;
    // 创建时间
    private LocalDateTime createTime;
} 