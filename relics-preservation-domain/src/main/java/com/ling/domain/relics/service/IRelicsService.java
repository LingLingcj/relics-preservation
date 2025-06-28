package com.ling.domain.relics.service;

import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.model.entity.RelicsEntity;

public interface IRelicsService {
    /**
     * 上传文物
     * @param vo 文物值对象
     * @return 文物实体
     */
    RelicsEntity uploadRelics(RelicsVO vo);
}
