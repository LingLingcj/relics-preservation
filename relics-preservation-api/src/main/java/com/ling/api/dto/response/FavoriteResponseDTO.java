package com.ling.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponseDTO {
    private Long id;
    private Long relicsId;
    private String relicsName;
    private String relicsImageUrl;
    private String relicsDescription;
    private LocalDateTime createTime;
} 