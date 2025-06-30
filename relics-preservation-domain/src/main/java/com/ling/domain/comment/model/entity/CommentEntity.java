package com.ling.domain.comment.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description: 文物评论实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    private Long id;
    private Long relicsId;
    private String username;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;
} 