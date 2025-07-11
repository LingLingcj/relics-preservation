package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审核统计DTO
 * @Author: LingRJ
 * @Description: 审核人员统计信息的DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "审核统计DTO")
public class ReviewStatisticsDTO {
    
    @Schema(description = "审核人")
    private String reviewer;
    
    @Schema(description = "统计开始时间")
    private LocalDateTime startTime;
    
    @Schema(description = "统计结束时间")
    private LocalDateTime endTime;
    
    @Schema(description = "总审核数量")
    private Long totalReviewed;
    
    @Schema(description = "通过数量")
    private Long approvedCount;
    
    @Schema(description = "拒绝数量")
    private Long rejectedCount;
    
    @Schema(description = "通过率")
    private Double approvalRate;
    
    @Schema(description = "拒绝率")
    private Double rejectionRate;
    
    @Schema(description = "每日统计")
    private Map<String, Long> dailyStats;
    
    @Schema(description = "平均审核时间（分钟）")
    private Double avgReviewTime;
    
    @Schema(description = "审核效率等级")
    private String efficiencyLevel;
}
