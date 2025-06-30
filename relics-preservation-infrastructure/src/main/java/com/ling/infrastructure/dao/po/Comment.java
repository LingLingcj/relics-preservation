package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description: 文物评论持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private Long relicsId;
    private String username;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;
} 