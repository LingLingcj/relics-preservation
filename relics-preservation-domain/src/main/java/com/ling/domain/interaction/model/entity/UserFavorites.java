package com.ling.domain.interaction.model.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.ling.domain.interaction.event.UserFavoritedRelicsEvent;
import com.ling.domain.interaction.event.UserUnfavoritedRelicsEvent;
import com.ling.domain.interaction.model.valobj.ChangeTracker;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.interaction.model.valobj.InteractionResult;
import com.ling.domain.user.event.DomainEventPublisher;
import com.ling.domain.user.model.valobj.Username;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户收藏聚合根
 * @Author: LingRJ
 * @Description: 专门管理用户收藏功能的聚合根，负责收藏/取消收藏操作、收藏列表查询等
 * @DateTime: 2025/7/13
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Slf4j
public class UserFavorites {

    private final Username username;
    private final Set<FavoriteAction> favorites;
    private final LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 变更跟踪器，用于增量保存
    private final ChangeTracker changeTracker;

    /**
     * 创建用户收藏聚合根
     */
    public static UserFavorites create(Username username) {
        LocalDateTime now = LocalDateTime.now();
        return UserFavorites.builder()
                .username(username)
                .favorites(new HashSet<>())
                .createTime(now)
                .updateTime(now)
                .changeTracker(new ChangeTracker())
                .build();
    }

    /**
     * 从数据库记录重建用户收藏聚合根
     * @param username 用户名
     * @param favorites 收藏行为集合
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 用户收藏聚合根
     */
    public static UserFavorites fromDatabase(Username username,
                                           Set<FavoriteAction> favorites,
                                           LocalDateTime createTime,
                                           LocalDateTime updateTime) {
        return UserFavorites.builder()
                .username(username)
                .favorites(favorites != null ? favorites : new HashSet<>())
                .createTime(createTime != null ? createTime : LocalDateTime.now())
                .updateTime(updateTime != null ? updateTime : LocalDateTime.now())
                .changeTracker(new ChangeTracker()) // 从数据库重建时创建新的变更跟踪器
                .build();
    }

    /**
     * 添加收藏
     * @param relicsId 文物ID
     * @return 操作结果
     */
    public InteractionResult addFavorite(Long relicsId) {
        try {
            FavoriteAction favorite = FavoriteAction.create(relicsId);

            // 检查是否已收藏
            if (favorites.contains(favorite)) {
                return InteractionResult.failure("已经收藏过该文物");
            }

            favorites.add(favorite);
            updateTime = LocalDateTime.now();

            // 记录变更
            changeTracker.recordAdd("FAVORITE", relicsId, favorite);

            // 发布收藏事件
            DomainEventPublisher.publish(new UserFavoritedRelicsEvent(
                    username.getValue(), relicsId));

            log.info("用户 {} 收藏文物 {}", username.getValue(), relicsId);
            return InteractionResult.success("收藏成功");

        } catch (Exception e) {
            log.error("添加收藏失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("收藏失败: " + e.getMessage());
        }
    }

    /**
     * 取消收藏
     * @param relicsId 文物ID
     * @return 操作结果
     */
    public InteractionResult removeFavorite(Long relicsId) {
        try {
            FavoriteAction favorite = FavoriteAction.create(relicsId);

            if (!favorites.contains(favorite)) {
                return InteractionResult.failure("未收藏该文物");
            }

            // 找到实际的收藏对象并标记删除
            Optional<FavoriteAction> existingFavorite = favorites.stream()
                    .filter(f -> f.equals(favorite))
                    .findFirst();

            if (existingFavorite.isPresent()) {
                FavoriteAction actualFavorite = existingFavorite.get();
                actualFavorite.delete();
                updateTime = LocalDateTime.now();

                // 记录变更
                changeTracker.recordDelete("FAVORITE", relicsId, actualFavorite);

                // 发布取消收藏事件
                DomainEventPublisher.publish(new UserUnfavoritedRelicsEvent(
                        username.getValue(), relicsId));

                log.info("用户 {} 取消收藏文物 {}", username.getValue(), relicsId);
                return InteractionResult.success("取消收藏成功");
            }

            return InteractionResult.failure("取消收藏失败");

        } catch (Exception e) {
            log.error("取消收藏失败: {} - {}", username.getValue(), e.getMessage(), e);
            return InteractionResult.failure("取消收藏失败: " + e.getMessage());
        }
    }

    /**
     * 检查是否已收藏文物
     * @param relicsId 文物ID
     * @return 是否已收藏
     */
    public boolean isFavorited(Long relicsId) {
        return favorites.contains(FavoriteAction.create(relicsId));
    }

    /**
     * 获取收藏的文物ID列表
     * @return 文物ID列表
     */
    public List<Long> getFavoritedRelicsIds() {
        return favorites.stream()
                .filter(f -> !f.isDeleted())
                .map(FavoriteAction::getRelicsId)
                .sorted()
                .toList();
    }

    /**
     * 获取收藏统计信息
     * @return 收藏统计
     */
    public FavoriteStatistics getStatistics() {
        long favoriteCount = favorites.stream()
                .filter(f -> !f.isDeleted())
                .count();

        long newFavoriteCount = favorites.stream()
                .filter(f -> !f.isDeleted() && f.isNewFavorite())
                .count();

        return FavoriteStatistics.builder()
                .username(username.getValue())
                .totalFavorites(favoriteCount)
                .newFavorites(newFavoriteCount)
                .lastFavoriteTime(updateTime)
                .build();
    }

    /**
     * 获取显示名称
     * @return 显示名称
     */
    public String getDisplayName() {
        return username.getValue();
    }

    // ==================== 增量保存相关方法 ====================

    /**
     * 检查是否有变更
     * @return 是否有变更
     */
    public boolean hasChanges() {
        return changeTracker.isHasChanges();
    }

    /**
     * 获取收藏变更记录
     * @return 收藏变更记录集合
     */
    public Set<ChangeTracker.ChangeRecord> getFavoriteChanges() {
        return changeTracker.getChangesByType("FAVORITE");
    }

    /**
     * 清空变更记录（保存成功后调用）
     */
    public void clearChanges() {
        changeTracker.clearChanges();
    }

    /**
     * 获取变更统计信息
     * @return 变更统计信息
     */
    public String getChangesSummary() {
        if (!hasChanges()) {
            return "无变更";
        }

        int favoriteChanges = changeTracker.getChangeCount("FAVORITE");
        return String.format("收藏变更: %d", favoriteChanges);
    }

    /**
     * 收藏统计值对象
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    public static class FavoriteStatistics {
        private final String username;
        private final long totalFavorites;
        private final long newFavorites;
        private final LocalDateTime lastFavoriteTime;
    }
}
