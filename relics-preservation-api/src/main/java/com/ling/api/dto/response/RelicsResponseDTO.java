package com.ling.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: LingRJ
 * @Description: 文物响应DTO
 * @DateTime: 2025/6/30
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文物信息响应")
public class RelicsResponseDTO {

    @Schema(description = "文物名称")
    private String name;

    @Schema(description = "文物Id")
    private Long relicsId;
    
    @Schema(description = "文物详细描述")
    private String description;
    
    @Schema(description = "保护等级")
    private Integer preservation;
    
    @Schema(description = "类别") 
    private String category;
    
    @Schema(description = "所属年代")
    private String era;
    
    @Schema(description = "主要材质")
    private String material;
    
    @Schema(description = "文物图片链接")
    private String imageUrl;
    
    @Schema(description = "状态")
    private Integer status;
    
    @Schema(description = "所在位置ID")
    private Integer locationId;
} 