package com.ling.domain.relics.service.impl;

import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.service.IRelicsService;
import com.ling.domain.relics.service.IRelicsSearchService;
import com.ling.domain.relics.adapter.IRelicsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RelicsServiceImpl implements IRelicsService {
    @Autowired
    private IRelicsRepository relicsRepository;
    
    @Autowired
    private IRelicsSearchService relicsSearchService;

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
            
            // 同步到ES
            entity.setRelicsId(relicsId);
            try {
                relicsSearchService.syncRelicsToEs(entity);
                log.info("文物数据已同步到ES: {}", vo.getName());
            } catch (Exception e) {
                log.error("文物数据同步到ES失败: {}", e.getMessage(), e);
                // 即使同步ES失败，不影响主流程
            }
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
        try {
            RelicsEntity entity = relicsRepository.findById(id);
            if (entity != null) {
                entity.setSuccess(true);
                entity.setMessage("查询成功");
            }
            return entity;
        } catch (Exception e) {
            RelicsEntity errorEntity = new RelicsEntity();
            errorEntity.setSuccess(false);
            errorEntity.setMessage("查询文物失败: " + e.getMessage());
            return errorEntity;
        }
    }
    
    @Override
    public List<RelicsEntity> getRelicsExceptEras(List<String> excludeEras) {
        try {
            return relicsRepository.findRelicsExceptEras(excludeEras);
        } catch (Exception e) {
            // 记录异常并返回空列表
            return List.of();
        }
    }
    
    @Override
    public List<RelicsEntity> getRelicsByName(String name) {
        try {
            return relicsRepository.findByNameContaining(name);
        } catch (Exception e) {
            // 返回空列表
            return List.of();
        }
    }
}
