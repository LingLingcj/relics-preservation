package com.ling.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Author: LingRJ
 * @Description: 评论添加DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加评论请求DTO")
public class CommentAddDTO {
    
    @NotNull(message = "文物ID不能为空")
    @Schema(description = "文物ID", example = "1")
    private Long relicsId;
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字符之间")
    @Schema(description = "评论内容", example = "这件文物保存得非常完好，工艺精湛。")
    private String content;
} 