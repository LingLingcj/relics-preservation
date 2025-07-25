package com.ling.domain.relics.service;

import com.ling.domain.relics.model.entity.RelicsEntity;

import java.util.List;

/**
 * 文物搜索服务接口
 */
public interface IRelicsSearchService {
    
    /**
     * 根据文物名称搜索文物
     * @param name 文物名称关键字
     * @return 文物实体列表
     */
    List<RelicsEntity> searchRelicsByName(String name);
    
    /**
     * 多字段搜索文物
     * 搜索名称、朝代、类别、描述、材质等字段
     * @param keyword 搜索关键词
     * @return 文物实体列表
     */
    List<RelicsEntity> searchRelicsByKeyword(String keyword);
    
    /**
     * 同步单个文物到ES
     * @param relicsEntity 文物实体
     * @return 同步结果
     */
    boolean syncRelicsToEs(RelicsEntity relicsEntity);
    
    /**
     * 同步所有文物到ES
     * @return 同步结果
     */
    boolean syncAllRelicsToEs();
} 