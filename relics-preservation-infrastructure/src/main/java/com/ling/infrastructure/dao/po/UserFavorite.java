package com.ling.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户收藏持久化对象
 * @Author: LingRJ
 * @Description: 用户收藏文物的数据库映射对象
 * @DateTime: 2025/7/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavorite {
    
    /** 自增主键 */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 文物ID */
    private Long relicsId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 状态：0-正常，1-已删除 */
    private Integer status;
}
