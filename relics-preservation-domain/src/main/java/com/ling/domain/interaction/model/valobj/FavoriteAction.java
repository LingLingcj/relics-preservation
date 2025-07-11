package com.ling.domain.interaction.model.valobj;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 收藏行为值对象
 * @Author: LingRJ
 * @Description: 封装用户收藏文物的行为
 * @DateTime: 2025/7/11
 */
@Getter
@EqualsAndHashCode(of = "relicsId")
public class FavoriteAction {
    
    private final Long relicsId;
    private final LocalDateTime createTime;
    private boolean deleted;
    
    private FavoriteAction(Long relicsId) {
        this.relicsId = Objects.requireNonNull(relicsId, "文物ID不能为空");
        this.createTime = LocalDateTime.now();
        this.deleted = false;
        
        if (relicsId <= 0) {
            throw new IllegalArgumentException("文物ID必须大于0");
        }
    }
    
    /**
     * 创建收藏行为
     * @param relicsId 文物ID
     * @return 收藏行为值对象
     */
    public static FavoriteAction create(Long relicsId) {
        return new FavoriteAction(relicsId);
    }
    
    /**
     * 标记为已删除
     */
    public void delete() {
        this.deleted = true;
    }
    
    /**
     * 获取收藏时长（天数）
     * @return 收藏天数
     */
    public long getFavoriteDays() {
        return java.time.Duration.between(createTime, LocalDateTime.now()).toDays();
    }
    
    /**
     * 是否为新收藏（24小时内）
     * @return 是否为新收藏
     */
    public boolean isNewFavorite() {
        return getFavoriteDays() == 0;
    }
    
    @Override
    public String toString() {
        return String.format("FavoriteAction{relicsId=%d, createTime=%s, deleted=%s}", 
                relicsId, createTime, deleted);
    }
}
