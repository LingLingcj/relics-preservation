package com.ling.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import org.jetbrains.annotations.NotNull;

/**
 * @Description: 评论查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论查询请求DTO")
public class CommentQueryDTO {
    
    @NotNull(value = "文物ID不能为空")
    @Schema(description = "文物ID", example = "1")
    private Long relicsId;
    
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码，从1开始", example = "1", defaultValue = "1")
    private Integer page = 1;
    
    @Min(value = 1, message = "每页大小不能小于1")
    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    private Integer size = 10;
} 