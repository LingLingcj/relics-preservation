package com.ling.domain.interaction.adapter;

import java.util.List;
import java.util.Optional;

import com.ling.domain.interaction.model.entity.UserFavorites;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.interaction.service.IUserInteractionService;
import com.ling.domain.user.model.valobj.Username;

/**
 * 用户收藏仓储接口
 * @Author: LingRJ
 * @Description: 用户收藏聚合根的数据访问接口
 * @DateTime: 2025/7/13
 */
public interface IUserFavoritesRepository {

    /**
     * 根据用户名查找用户收藏聚合根
     * @param username 用户名
     * @return 用户收藏聚合根
     */
    Optional<UserFavorites> findByUsername(Username username);

    /**
     * 保存用户收藏聚合根（增量保存）
     * @param userFavorites 用户收藏聚合根
     * @return 保存是否成功
     */
    boolean saveIncremental(UserFavorites userFavorites);

    /**
     * 保存用户收藏聚合根（全量保存）
     * @param userFavorites 用户收藏聚合根
     * @return 保存是否成功
     */
    boolean save(UserFavorites userFavorites);

    /**
     * 删除用户收藏聚合根
     * @param username 用户名
     * @return 删除是否成功
     */
    boolean deleteByUsername(Username username);

    /**
     * 检查用户是否存在收藏记录
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(Username username);

}
