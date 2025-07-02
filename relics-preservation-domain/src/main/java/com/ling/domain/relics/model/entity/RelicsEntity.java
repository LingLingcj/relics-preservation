package com.ling.domain.relics.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文物领域实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelicsEntity implements Serializable {
    private String name;
    private String description;
    private String location;
    private String imageUrl;

    private Long relicsId;

    /** 保护等级 */
    private Integer preservation;
    /** 类别 */
    private String category;
    /** 所属年代 */
    private String era;
    /** 主要材质 */
    private String material;
    /** 状态 */
    private Integer status;
    /** 所在位置ID */
    private Integer locationId;

    private boolean success;
    private String message;

}

