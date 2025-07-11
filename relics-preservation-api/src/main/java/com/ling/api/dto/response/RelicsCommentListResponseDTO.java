package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文物评论列表响应DTO
 * @Author: LingRJ
 * @Description: 文物评论分页查询结果
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文物评论列表响应")
public class RelicsCommentListResponseDTO {
    
    @Schema(description = "评论列表")
    private List<RelicsCommentDTO> comments;
    
    @Schema(description = "总评论数", example = "25")
    private Long totalCount;
    
    @Schema(description = "当前页码", example = "1")
    private Integer currentPage;
    
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;
    
    @Schema(description = "总页数", example = "3")
    private Integer totalPages;
    
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;
    
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;
    
    @Schema(description = "文物ID", example = "1")
    private Long relicsId;
    
    @Schema(description = "分页信息摘要", example = "第 1-10 条，共 25 条评论")
    private String paginationSummary;
}
