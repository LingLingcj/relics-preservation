package com.ling.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 收藏请求DTO
 * @Author: LingRJ
 * @Description: 用户收藏操作的请求DTO
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏请求DTO")
public class FavoriteRequestDTO {
    
    @NotNull(message = "文物ID不能为空")
    @Positive(message = "文物ID必须为正数")
    @Schema(description = "文物ID", example = "1", required = true)
    private Long relicsId;
    
    @Schema(description = "操作类型：true-收藏，false-取消收藏", example = "true")
    private Boolean favorite = true;
}
