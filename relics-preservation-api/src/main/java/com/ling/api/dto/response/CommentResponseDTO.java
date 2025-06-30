package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Description: 评论响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论响应DTO")
public class CommentResponseDTO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "文物ID")
    private Long relicsId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "是否为当前用户的评论")
    private Boolean isOwner;
}