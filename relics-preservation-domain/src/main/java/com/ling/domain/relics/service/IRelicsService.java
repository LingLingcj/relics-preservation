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
}
