package com.ling.infrastructure.repository;

import com.ling.domain.relics.adapter.IRelicsRepository;
import com.ling.domain.relics.model.entity.RelicsEntity;
import com.ling.domain.relics.model.valobj.RelicsVO;
import com.ling.infrastructure.dao.IRelicsDao;
import com.ling.infrastructure.dao.po.Relics;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class RelicsRepositoryImpl implements IRelicsRepository {
    @Autowired
    private IRelicsDao relicsDao;

    @Override
    public void uploadRelics(RelicsVO relicsVO) {
        Relics relics = new Relics();
        BeanUtils.copyProperties(relicsVO, relics);
        relicsDao.insertRelics(relics);
    }

    @Override
    public RelicsEntity findById(Long id) {
        // TODO: 需要实现根据ID查询PO并转为Entity
        return null;
    }

    @Override
    public boolean existsByName(String name) {
        // TODO: 需要实现根据名称判断是否存在
        return false;
    }

    @Override
    public boolean save(RelicsEntity relicsEntity) {
        Relics relics = new Relics();
        BeanUtils.copyProperties(relicsEntity, relics);
        return relicsDao.insertRelics(relics) > 0;
    }

    @Override
    public boolean updateRelics(RelicsEntity relicsEntity) {
        // TODO: 需要实现更新逻辑
        return false;
    }

    @Override
    public List<RelicsEntity> findByEra(String era) {
        List<Relics> relicsList = relicsDao.selectByEra(era);
        return relicsList.stream().map(relics -> {
            RelicsEntity relicsEntity = new RelicsEntity();
            BeanUtils.copyProperties(relics, relicsEntity);
            relicsEntity.setSuccess(true);
            return relicsEntity;
        }).collect(Collectors.toList());
    }
    
    @Override
    public List<RelicsEntity> findRandomRelics(int limit) {
        List<Relics> relicsList = relicsDao.selectRandomRelics(limit);
        return relicsList.stream().map(relics -> {
            RelicsEntity relicsEntity = new RelicsEntity();
            BeanUtils.copyProperties(relics, relicsEntity);
            relicsEntity.setSuccess(true);
            return relicsEntity;
        }).collect(Collectors.toList());
    }
}
