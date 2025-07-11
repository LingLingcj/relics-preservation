package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 交互统计响应DTO
 * @Author: LingRJ
 * @Description: 用户或文物交互统计的响应DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "交互统计响应DTO")
public class InteractionStatisticsResponseDTO {
    
    @Schema(description = "用户名（用户统计时使用）")
    private String username;
    
    @Schema(description = "文物ID（文物统计时使用）")
    private Long relicsId;
    
    @Schema(description = "文物名称（文物统计时使用）")
    private String relicsName;
    
    @Schema(description = "收藏数量")
    private Long favoriteCount;
    
    @Schema(description = "评论数量")
    private Long commentCount;
    
    @Schema(description = "总交互次数")
    private Long totalInteractions;
    
    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveTime;
    
    @Schema(description = "首次交互时间")
    private LocalDateTime firstInteractionTime;
    
    @Schema(description = "活跃度等级")
    private String activityLevel;
    
    @Schema(description = "活跃度等级描述")
    private String activityLevelDescription;
    
    @Schema(description = "热度分数（文物统计时使用）")
    private Double popularityScore;
    
    @Schema(description = "是否为活跃用户")
    private Boolean isActiveUser;
    
    @Schema(description = "是否为新用户")
    private Boolean isNewUser;
    
    @Schema(description = "收藏评论比例")
    private Double favoriteCommentRatio;
}
