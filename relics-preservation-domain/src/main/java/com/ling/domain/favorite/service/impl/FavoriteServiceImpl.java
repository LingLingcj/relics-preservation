package com.ling.domain.favorite.service.impl;

import com.ling.domain.favorite.adapter.IFavoriteRepository;
import com.ling.domain.favorite.model.entity.FavoriteEntity;
import com.ling.domain.favorite.service.IFavoriteService;
import com.ling.domain.relics.adapter.IRelicsRepository;
import com.ling.domain.relics.model.entity.RelicsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏服务实现
 */
@Service
public class FavoriteServiceImpl implements IFavoriteService {

    @Autowired
    private IFavoriteRepository favoriteRepository;
    
    @Autowired
    private IRelicsRepository relicsRepository;

    @Override
    @Transactional
    public FavoriteEntity addFavorite(Long relicsId, String username) {
        // 判断是否已收藏
        if (favoriteRepository.isFavorite(username, relicsId)) {
            return FavoriteEntity.builder()
                    .success(false)
                    .message("您已收藏过此文物")
                    .build();
        }
        
        // 判断文物是否存在
        RelicsEntity relicsEntity = relicsRepository.findById(relicsId);
        if (relicsEntity == null) {
            return FavoriteEntity.builder()
                    .success(false)
                    .message("文物不存在")
                    .build();
        }
        
        // 创建收藏记录
        FavoriteEntity favoriteEntity = FavoriteEntity.builder()
                .relicsId(relicsId)
                .username(username)
                .createTime(LocalDateTime.now())
                .build();
        
        // 添加收藏
        boolean result = favoriteRepository.add(favoriteEntity);
        
        if (result) {
            favoriteEntity.setSuccess(true);
            favoriteEntity.setMessage("收藏成功");
        } else {
            favoriteEntity.setSuccess(false);
            favoriteEntity.setMessage("收藏失败");
        }
        
        return favoriteEntity;
    }

    @Override
    @Transactional
    public FavoriteEntity cancelFavorite(Long relicsId, String username) {
        // 判断是否已收藏
        if (!favoriteRepository.isFavorite(username, relicsId)) {
            return FavoriteEntity.builder()
                    .success(false)
                    .message("您未收藏此文物")
                    .build();
        }
        
        // 取消收藏
        boolean result = favoriteRepository.cancel(username, relicsId);
        
        FavoriteEntity favoriteEntity = new FavoriteEntity();
        favoriteEntity.setRelicsId(relicsId);
        favoriteEntity.setUsername(username);
        
        if (result) {
            favoriteEntity.setSuccess(true);
            favoriteEntity.setMessage("取消收藏成功");
        } else {
            favoriteEntity.setSuccess(false);
            favoriteEntity.setMessage("取消收藏失败");
        }
        
        return favoriteEntity;
    }

    @Override
    public boolean isFavorite(Long relicsId, String username) {
        return favoriteRepository.isFavorite(username, relicsId);
    }

    @Override
    public List<FavoriteEntity> getUserFavorites(String username, int page, int size) {
        return favoriteRepository.getUserFavorites(username, page, size);
    }
    
    @Override
    public int countUserFavorites(String username) {
        return favoriteRepository.countUserFavorites(username);
    }
} 