package com.ling.infrastructure.repository.converter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ling.domain.interaction.model.entity.UserFavorites;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.dao.po.UserFavorite;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户收藏数据转换器
 * @Author: LingRJ
 * @Description: 负责UserFavorites聚合根与数据库记录之间的转换
 * @DateTime: 2025/7/13
 */
@Slf4j
@Component
public class UserFavoritesConverter {

    /**
     * 构建用户收藏聚合根
     * @param username 用户名
     * @param favorites 收藏记录列表
     * @return 用户收藏聚合根
     */
    public UserFavorites buildUserFavorites(Username username, List<UserFavorite> favorites) {
        try {
            log.debug("构建用户收藏聚合根: {}, 收藏数: {}", username.getValue(), favorites.size());

            // 转换收藏数据
            Set<FavoriteAction> favoriteActions = convertFavorites(favorites);

            // 计算时间信息
            LocalDateTime earliestTime = calculateEarliestTime(favorites);
            LocalDateTime latestTime = calculateLatestTime(favorites);

            // 使用工厂方法重建聚合根
            return UserFavorites.fromDatabase(username, favoriteActions, earliestTime, latestTime);

        } catch (Exception e) {
            log.error("构建用户收藏聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            throw new RuntimeException("构建用户收藏聚合根失败", e);
        }
    }

    /**
     * 转换收藏记录列表
     * @param favorites 数据库收藏记录
     * @return 收藏行为值对象集合
     */
    public Set<FavoriteAction> convertFavorites(List<UserFavorite> favorites) {
        Set<FavoriteAction> actions = new HashSet<>();
        for (UserFavorite favorite : favorites) {
            try {
                FavoriteAction action = convertToFavoriteAction(favorite);
                actions.add(action);
            } catch (Exception e) {
                log.warn("转换收藏记录失败: {} - {}", favorite.getId(), e.getMessage());
            }
        }
        return actions;
    }

    /**
     * 从数据库记录转换为收藏行为
     * @param favorite 数据库收藏记录
     * @return 收藏行为值对象
     */
    public FavoriteAction convertToFavoriteAction(UserFavorite favorite) {
        return FavoriteAction.fromDatabase(
                favorite.getRelicsId(),
                favorite.getCreateTime(),
                favorite.getStatus() == 1 // status=1表示已删除
        );
    }

    /**
     * 将聚合根转换为数据库记录列表
     * @param userFavorites 用户收藏聚合根
     * @return 数据库收藏记录列表
     */
    public List<UserFavorite> convertToUserFavorites(UserFavorites userFavorites) {
        List<UserFavorite> favorites = new ArrayList<>();
        
        for (FavoriteAction favoriteAction : userFavorites.getFavorites()) {
            try {
                UserFavorite userFavorite = convertToUserFavorite(userFavorites.getUsername(), favoriteAction);
                favorites.add(userFavorite);
            } catch (Exception e) {
                log.warn("转换收藏行为失败: {} - {}", 
                        favoriteAction.getRelicsId(), e.getMessage());
            }
        }
        
        return favorites;
    }

    /**
     * 将收藏行为转换为数据库记录
     * @param username 用户名
     * @param favoriteAction 收藏行为
     * @return 数据库收藏记录
     */
    public UserFavorite convertToUserFavorite(Username username, FavoriteAction favoriteAction) {
        LocalDateTime now = LocalDateTime.now();
        
        return UserFavorite.builder()
                .username(username.getValue())
                .relicsId(favoriteAction.getRelicsId())
                .createTime(favoriteAction.getCreateTime())
                .updateTime(now)
                .status(favoriteAction.isDeleted() ? 1 : 0)
                .build();
    }

    /**
     * 计算最早时间
     * @param favorites 收藏记录列表
     * @return 最早时间
     */
    private LocalDateTime calculateEarliestTime(List<UserFavorite> favorites) {
        LocalDateTime earliest = null;

        for (UserFavorite favorite : favorites) {
            if (earliest == null || favorite.getCreateTime().isBefore(earliest)) {
                earliest = favorite.getCreateTime();
            }
        }

        return earliest != null ? earliest : LocalDateTime.now();
    }

    /**
     * 计算最晚时间
     * @param favorites 收藏记录列表
     * @return 最晚时间
     */
    private LocalDateTime calculateLatestTime(List<UserFavorite> favorites) {
        LocalDateTime latest = null;

        for (UserFavorite favorite : favorites) {
            LocalDateTime time = favorite.getUpdateTime() != null ? 
                    favorite.getUpdateTime() : favorite.getCreateTime();
            if (latest == null || time.isAfter(latest)) {
                latest = time;
            }
        }

        return latest != null ? latest : LocalDateTime.now();
    }

    /**
     * 构建收藏统计信息
     * @param username 用户名
     * @param favorites 收藏记录列表
     * @return 收藏统计
     */
    public UserFavorites.FavoriteStatistics buildFavoriteStatistics(Username username, List<UserFavorite> favorites) {
        long totalFavorites = favorites.stream()
                .filter(f -> f.getStatus() == 0) // 只统计正常状态的收藏
                .count();

        long newFavorites = favorites.stream()
                .filter(f -> f.getStatus() == 0)
                .filter(f -> isNewFavorite(f.getCreateTime()))
                .count();

        LocalDateTime lastFavoriteTime = calculateLatestTime(favorites);

        return UserFavorites.FavoriteStatistics.builder()
                .username(username.getValue())
                .totalFavorites(totalFavorites)
                .newFavorites(newFavorites)
                .lastFavoriteTime(lastFavoriteTime)
                .build();
    }

    /**
     * 判断是否为新收藏（24小时内）
     * @param createTime 创建时间
     * @return 是否为新收藏
     */
    private boolean isNewFavorite(LocalDateTime createTime) {
        if (createTime == null) {
            return false;
        }
        
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return createTime.isAfter(oneDayAgo);
    }

    /**
     * 验证收藏记录的有效性
     * @param favorite 收藏记录
     * @return 是否有效
     */
    public boolean isValidFavorite(UserFavorite favorite) {
        return favorite != null 
                && favorite.getUsername() != null 
                && !favorite.getUsername().trim().isEmpty()
                && favorite.getRelicsId() != null 
                && favorite.getRelicsId() > 0
                && favorite.getCreateTime() != null;
    }

    /**
     * 过滤有效的收藏记录
     * @param favorites 收藏记录列表
     * @return 有效的收藏记录列表
     */
    public List<UserFavorite> filterValidFavorites(List<UserFavorite> favorites) {
        return favorites.stream()
                .filter(this::isValidFavorite)
                .toList();
    }

    /**
     * 构建收藏活动描述
     * @param favorite 收藏记录
     * @return 活动描述
     */
    public String buildFavoriteActivityDescription(UserFavorite favorite) {
        if (favorite.getStatus() == 1) {
            return "取消收藏文物";
        } else {
            return "收藏了文物";
        }
    }

    /**
     * 检查收藏记录是否需要更新
     * @param existing 现有记录
     * @param favoriteAction 收藏行为
     * @return 是否需要更新
     */
    public boolean needsUpdate(UserFavorite existing, FavoriteAction favoriteAction) {
        if (existing == null || favoriteAction == null) {
            return false;
        }

        // 检查状态是否需要更新
        int expectedStatus = favoriteAction.isDeleted() ? 1 : 0;
        return existing.getStatus() != expectedStatus;
    }
}
