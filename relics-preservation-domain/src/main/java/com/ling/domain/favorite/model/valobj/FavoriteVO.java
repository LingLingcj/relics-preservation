package com.ling.domain.favorite.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteVO {
    private Long id;
    private Long relicsId;
    private String username;
    private LocalDateTime createTime;
    // 操作结果
    private boolean success;
    private String message;
} 