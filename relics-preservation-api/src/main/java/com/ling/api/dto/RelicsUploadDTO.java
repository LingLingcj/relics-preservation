package com.ling.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文物上传DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelicsUploadDTO {
    /** 文物名称 */
    private String name;
    /** 文物详细描述 */
    private String description;
    /** 保护等级 */
    private Integer preservation;
    /** 类别 */
    private String category;
    /** 所属年代 */
    private String era;
    /** 主要材质 */
    private String material;
    /** 文物图片链接 */
    private String imageUrl;
    /** 状态 */
    private Integer status;
    /** 所在位置ID */
    private Integer locationId;
}
