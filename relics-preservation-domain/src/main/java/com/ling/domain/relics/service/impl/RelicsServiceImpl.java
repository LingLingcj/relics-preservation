package com.ling.domain.relics.service.impl;

import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.domain.relics.adapter.IRelicsRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RelicsServiceImpl implements IRelicsService {
    @Autowired
    private IRelicsRepository relicsRepository;

    @Override
    public RelicsEntity uploadRelics(RelicsVO vo) {
        RelicsEntity entity = new RelicsEntity();
        BeanUtils.copyProperties(vo, entity);
        
        // 使用UUID的最后12位数字（去掉所有非数字字符后）转为Long类型
        String uuid = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
        Long relicsId = Long.parseLong(uuid.substring(0, Math.min(uuid.length(), 18)));
        
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

    @Override
    public List<RelicsEntity> getRelicsByEra(String era) {
        return relicsRepository.findByEra(era);
    }
    
    @Override
    public List<RelicsEntity> getRandomRelics(int count) {
        return relicsRepository.findRandomRelics(count);
    }

    @Override
    public RelicsEntity getRelicsById(Long id) {
        return relicsRepository.findById(id);
    }
}
