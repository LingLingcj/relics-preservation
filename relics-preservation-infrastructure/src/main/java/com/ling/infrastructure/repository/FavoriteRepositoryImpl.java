package com.ling.infrastructure.repository;

import com.ling.domain.favorite.adapter.IFavoriteRepository;
import com.ling.domain.favorite.model.entity.FavoriteEntity;
import com.ling.infrastructure.dao.IFavoriteDao;
import com.ling.infrastructure.dao.po.Favorite;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏仓储实现类
 */
@Repository
public class FavoriteRepositoryImpl implements IFavoriteRepository {

    @Autowired
    private IFavoriteDao favoriteDao;

    @Override
    public boolean add(FavoriteEntity favoriteEntity) {
        Favorite favorite = new Favorite();
        BeanUtils.copyProperties(favoriteEntity, favorite);
        return favoriteDao.insertFavorite(favorite) > 0;
    }

    @Override
    public boolean cancel(String username, Long relicsId) {
        return favoriteDao.deleteFavorite(username, relicsId) > 0;
    }

    @Override
    public boolean isFavorite(String username, Long relicsId) {
        return favoriteDao.findByUsernameAndRelicsId(username, relicsId) != null;
    }

    @Override
    public List<FavoriteEntity> getUserFavorites(String username, int page, int size) {
        int offset = (page - 1) * size;
        List<Favorite> favorites = favoriteDao.findByUsername(username, offset, size);
        
        return favorites.stream().map(favorite -> {
            FavoriteEntity favoriteEntity = new FavoriteEntity();
            BeanUtils.copyProperties(favorite, favoriteEntity);
            return favoriteEntity;
        }).collect(Collectors.toList());
    }
    
    @Override
    public int countUserFavorites(String username) {
        return favoriteDao.countByUsername(username);
    }
}