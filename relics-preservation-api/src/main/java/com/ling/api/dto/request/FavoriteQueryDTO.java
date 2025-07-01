package com.ling.api.dto.request;

import lombok.Data;

/**
 * 收藏查询DTO
 */
@Data
public class FavoriteQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
} 