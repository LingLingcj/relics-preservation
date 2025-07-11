package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论审核响应DTO
 * @Author: LingRJ
 * @Description: 评论审核操作的响应DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论审核响应DTO")
public class CommentReviewResponseDTO {
    
    @Schema(description = "评论ID")
    private Long commentId;
    
    @Schema(description = "审核操作")
    private String action;
    
    @Schema(description = "审核人")
    private String reviewer;
    
    @Schema(description = "审核理由")
    private String reason;
    
    @Schema(description = "审核时间")
    private LocalDateTime reviewTime;
    
    @Schema(description = "审核前状态")
    private String beforeStatus;
    
    @Schema(description = "审核后状态")
    private String afterStatus;
    
    @Schema(description = "是否成功")
    private Boolean success;
    
    @Schema(description = "结果消息")
    private String message;
}
