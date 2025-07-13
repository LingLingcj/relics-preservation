package com.ling.domain.interaction.model.valobj;

import java.time.LocalDateTime;
import java.util.Objects;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 收藏行为值对象
 * @Author: LingRJ
 * @Description: 封装用户收藏文物的行为
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
@NoArgsConstructor(force = true)
@EqualsAndHashCode(of = {"relicsId", "deleted"})
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
     * 从数据库重建的包私有构造方法
     */
    FavoriteAction(Long relicsId, LocalDateTime createTime, boolean deleted) {
        this.relicsId = Objects.requireNonNull(relicsId, "文物ID不能为空");
        this.createTime = Objects.requireNonNull(createTime, "创建时间不能为空");
        this.deleted = deleted;

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
     * 从数据库记录重建收藏行为
     * @param relicsId 文物ID
     * @param createTime 创建时间
     * @param deleted 是否已删除
     * @return 收藏行为值对象
     */
    public static FavoriteAction fromDatabase(Long relicsId, LocalDateTime createTime, boolean deleted) {
        return new FavoriteAction(relicsId, createTime, deleted);
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
