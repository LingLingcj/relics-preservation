package com.ling.domain.relics.adapter;

import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.model.valobj.RelicsVO;

import java.util.List;

/**
 * 文物仓储接口
 */
public interface IRelicsRepository {
    /**
     * 上传文物
     * @param relicsVO 文物值对象
     */
    void uploadRelics(RelicsVO relicsVO);

    /**
     * 根据ID查找文物
     * @param id 文物ID
     * @return 文物实体
     */
    RelicsEntity findById(Long id);

    /**
     * 判断文物是否存在
     * @param name 文物名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 保存文物
     * @param relicsEntity 文物实体
     * @return 保存结果
     */
    boolean save(RelicsEntity relicsEntity);

    /**
     * 更新文物信息
     * @param relicsEntity 文物实体
     * @return 更新结果
     */
    boolean updateRelics(RelicsEntity relicsEntity);

    /**
     * 根据年代查找文物
     * @param era 年代
     * @return 文物实体列表
     */
    List<RelicsEntity> findByEra(String era);

    /**
     * 随机获取文物列表
     * @param limit 获取数量
     * @return 文物实体列表
     */
    List<RelicsEntity> findRandomRelics(int limit);
}

