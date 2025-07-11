package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新评论响应DTO
 * @Author: LingRJ
 * @Description: 用户评论操作的响应DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "新评论响应DTO")
public class NewCommentResponseDTO {
    
    @Schema(description = "评论ID")
    private Long commentId;
    
    @Schema(description = "文物ID")
    private Long relicsId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "评论状态")
    private String status;
    
    @Schema(description = "评论状态描述")
    private String statusDescription;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "是否需要审核")
    private Boolean needsReview;
    
    @Schema(description = "是否为当前用户的评论")
    private Boolean isOwner;
}
