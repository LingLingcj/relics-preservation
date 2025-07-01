package com.ling.api.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 收藏添加DTO
 */
@Data
public class FavoriteAddDTO {
    @NotNull(message = "文物ID不能为空")
    private Long relicsId;
} 