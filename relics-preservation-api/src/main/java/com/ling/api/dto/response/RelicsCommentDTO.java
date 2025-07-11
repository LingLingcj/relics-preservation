package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文物评论响应DTO
 * @Author: LingRJ
 * @Description: 用于文物详情页展示的评论信息
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文物评论信息")
public class RelicsCommentDTO {
    
    @Schema(description = "评论ID", example = "1001")
    private Long commentId;
    
    @Schema(description = "用户名", example = "文物爱好者")
    private String username;
    
    @Schema(description = "评论内容", example = "这件文物保存得非常好，工艺精湛。")
    private String content;
    
    @Schema(description = "发表时间", example = "2025-07-11T10:30:00")
    private LocalDateTime createTime;
    
    @Schema(description = "点赞数", example = "5")
    private Integer likeCount;
    
    @Schema(description = "是否为精选评论", example = "false")
    private Boolean featured;
    
    @Schema(description = "是否为最近评论（24小时内）", example = "true")
    private Boolean recent;
    
    @Schema(description = "评论摘要（最多100字符）", example = "这件文物保存得非常好，工艺精湛。")
    private String contentSummary;
}
