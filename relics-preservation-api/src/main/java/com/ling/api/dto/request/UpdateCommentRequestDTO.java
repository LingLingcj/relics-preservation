package com.ling.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更新评论请求DTO
 * @Author: LingRJ
 * @Description: 统一的评论更新请求DTO
 * @DateTime: 2025/7/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新评论请求DTO")
public class UpdateCommentRequestDTO {
    
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字符之间")
    @Schema(description = "评论内容", example = "这件文物保存得非常完好，工艺精湛。", required = true)
    private String content;
}
