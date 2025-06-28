package com.ling.domain.relics.model.valobj;

import lombok.Data;
import java.util.Date;

/**
 * 文物VO
 */
@Data
public class RelicsVO {
    private Integer id;
    private String relicsId;
    private String name;
    private String description;
    private Integer preservation;
    private String category;
    private String era;
    private String material;
    private String imageUrl;
    private   Integer status;
    private Integer locationId;
    private Date createTime;
    private Date updateTime;
    // 上传结果
    private boolean success;
    private String message;
}
