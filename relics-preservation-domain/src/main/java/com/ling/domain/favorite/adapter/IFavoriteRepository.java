package com.ling.domain.favorite.adapter;

import com.ling.domain.favorite.model.entity.FavoriteEntity;

import java.util.List;

/**
 * 收藏仓储接口
 */
public interface IFavoriteRepository {
    /**
     * 添加收藏
     * @param favoriteEntity 收藏实体
     * @return 是否成功
     */
    boolean add(FavoriteEntity favoriteEntity);

    /**
     * 取消收藏
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 是否成功
     */
    boolean cancel(String username, Long relicsId);

    /**
     * 判断是否已收藏
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 是否已收藏
     */
    boolean isFavorite(String username, Long relicsId);

    /**
     * 获取用户收藏列表
     * @param username 用户名
     * @param page 页码
     * @param size 每页数量
     * @return 收藏列表
     */
    List<FavoriteEntity> getUserFavorites(String username, int page, int size);
    
    /**
     * 获取用户收藏总数
     * @param username 用户名
     * @return 收藏总数
     */
    int countUserFavorites(String username);
} 