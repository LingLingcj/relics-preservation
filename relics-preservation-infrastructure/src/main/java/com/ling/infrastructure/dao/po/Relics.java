package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: LingRJ
 * @Description: 文物实体
 * @DateTime: 2025/6/27 23:52
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Relics {
    /** 自增主键 */
    private Integer id;
    /** 文物业务ID */
    private String relicsId;
    /** 文物名称 */
    private String name;
    /** 文物详细描述 */
    private String description;
    /** 保护等级 */
    private Byte preservation;
    /** 类别 */
    private String category;
    /** 所属年代 */
    private String era;
    /** 主要材质 */
    private String material;
    /** 文物图片链接 */
    private String imageUrl;
    /** 状态 */
    private Byte status;
    /** 所在位置ID */
    private Integer locationId;
    /** 创建时间 */
    private Date createTime;
    /** 最后更新时间 */
    private Date updateTime;
}
