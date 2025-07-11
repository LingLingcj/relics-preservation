package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量审核响应DTO
 * @Author: LingRJ
 * @Description: 批量评论审核操作的响应DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量审核响应DTO")
public class BatchReviewResponseDTO {
    
    @Schema(description = "总数量")
    private Integer totalCount;
    
    @Schema(description = "成功数量")
    private Integer successCount;
    
    @Schema(description = "失败数量")
    private Integer failureCount;
    
    @Schema(description = "成功率")
    private Double successRate;
    
    @Schema(description = "是否全部成功")
    private Boolean allSuccess;
    
    @Schema(description = "错误信息列表")
    private List<String> errors;
}
