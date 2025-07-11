package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 待审核评论DTO
 * @Author: LingRJ
 * @Description: 待审核评论信息的DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "待审核评论DTO")
public class PendingCommentDTO {
    
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "文物ID")
    private Long relicsId;
    
    @Schema(description = "文物名称")
    private String relicsName;
    
    @Schema(description = "评论用户名")
    private String username;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "评论状态")
    private String status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "等待审核天数")
    private Long waitingDays;
    
    @Schema(description = "是否紧急")
    private Boolean urgent;
}
