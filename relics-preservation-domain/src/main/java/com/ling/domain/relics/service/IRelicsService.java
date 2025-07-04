package com.ling.domain.relics.service;

import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.model.entity.RelicsEntity;

import java.util.List;

public interface IRelicsService {
    /**
     * 上传文物
     * @param vo 文物值对象
     * @return 文物实体
     */
    RelicsEntity uploadRelics(RelicsVO vo);

    /**
     * 根据年代获取文物
     * @param era 年代
     * @return 文物实体列表
     */
    List<RelicsEntity> getRelicsByEra(String era);
    
    /**
     * 随机获取指定数量的文物
     * @param count 获取数量
     * @return 文物实体列表
     */
    List<RelicsEntity> getRandomRelics(int count);

    /**
     * 根据ID获取文物
     * @param id 文物ID
     * @return 文物实体
     */
    RelicsEntity getRelicsById(Long id);
    
    /**
     * 获取除指定朝代外的所有文物
     * @param excludeEras 要排除的朝代列表
     * @return 文物实体列表
     */
    List<RelicsEntity> getRelicsExceptEras(List<String> excludeEras);
    
    /**
     * 根据名称搜索文物
     * @param name 文物名称关键词
     * @return 文物实体列表
     */
    List<RelicsEntity> getRelicsByName(String name);
}
