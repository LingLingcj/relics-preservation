package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: LingRJ
 * @Description: 位置实体
 * @DateTime: 2025/6/27 23:50
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Location {
    // 自增主键
    private Integer id;
    // 位置名称
    private String name;
    // 位置详细描述
    private String description;
    // 父级位置ID
    private Integer parentId;
    // 创建时间
    private Date createTime;
    // 最后更新时间
    private Date updateTime;
}
