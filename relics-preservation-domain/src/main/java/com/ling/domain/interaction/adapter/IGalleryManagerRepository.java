package com.ling.domain.interaction.adapter;

import java.util.Optional;

import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.user.model.valobj.Username;

/**
 * 收藏馆管理仓储接口
 * @Author: LingRJ
 * @Description: 收藏馆管理聚合根的数据访问接口
 * @DateTime: 2025/7/13
 */
public interface IGalleryManagerRepository {

    /**
     * 根据用户名查找收藏馆管理聚合根
     * @param username 用户名
     * @return 收藏馆管理聚合根
     */
    Optional<GalleryManager> findByUsername(Username username);

    /**
     * 保存收藏馆管理聚合根（增量保存）
     * @param galleryManager 收藏馆管理聚合根
     * @return 保存是否成功
     */
    boolean saveIncremental(GalleryManager galleryManager);

    /**
     * 保存收藏馆管理聚合根（全量保存）
     * @param galleryManager 收藏馆管理聚合根
     * @return 保存是否成功
     */
    boolean save(GalleryManager galleryManager);

    /**
     * 删除收藏馆管理聚合根
     * @param username 用户名
     * @return 删除是否成功
     */
    boolean deleteByUsername(Username username);

    /**
     * 检查用户是否存在收藏馆记录
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(Username username);
}
