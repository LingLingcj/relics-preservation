package com.ling.domain.relics.service.impl;

import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.domain.relics.adapter.IRelicsRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RelicsServiceImpl implements IRelicsService {
    @Autowired
    private IRelicsRepository relicsRepository;

    @Override
    public RelicsEntity uploadRelics(RelicsVO vo) {
        RelicsEntity entity = new RelicsEntity();
        BeanUtils.copyProperties(vo, entity);
        String relicsId = RandomStringUtils.secure().nextAlphanumeric(10);
        vo.setRelicsId(relicsId);
        try {
            relicsRepository.uploadRelics(vo);
            entity.setSuccess(true);
            entity.setMessage("上传成功");
        } catch (Exception e) {
            entity.setSuccess(false);
            entity.setMessage("上传失败: " + e.getMessage());
        }
        return entity;
    }
}
