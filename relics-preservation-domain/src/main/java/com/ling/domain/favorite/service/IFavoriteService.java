package com.ling.domain.favorite.service;

import com.ling.domain.favorite.model.entity.FavoriteEntity;
import com.ling.domain.favorite.model.valobj.FavoriteVO;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface IFavoriteService {
    /**
     * 添加收藏
     * @param relicsId 文物ID
     * @param username 用户名
     * @return 收藏结果
     */
    FavoriteEntity addFavorite(Long relicsId, String username);

    /**
     * 取消收藏
     * @param relicsId 文物ID
     * @param username 用户名
     * @return 取消结果
     */
    FavoriteEntity cancelFavorite(Long relicsId, String username);

    /**
     * 判断是否已收藏
     * @param relicsId 文物ID
     * @param username 用户名
     * @return 是否已收藏
     */
    boolean isFavorite(Long relicsId, String username);

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