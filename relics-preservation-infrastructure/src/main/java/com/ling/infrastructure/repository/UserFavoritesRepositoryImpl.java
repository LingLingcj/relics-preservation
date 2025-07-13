package com.ling.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ling.domain.interaction.adapter.IUserFavoritesRepository;
import com.ling.domain.interaction.model.entity.UserFavorites;
import com.ling.domain.interaction.model.valobj.ChangeTracker;
import com.ling.domain.interaction.model.valobj.FavoriteAction;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.service.UserFavoritesCacheService;
import com.ling.infrastructure.dao.IUserFavoriteDao;
import com.ling.infrastructure.dao.po.UserFavorite;
import com.ling.infrastructure.repository.converter.UserFavoritesConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户收藏仓储实现
 * @Author: LingRJ
 * @Description: 实现用户收藏聚合根的数据访问逻辑
 * @DateTime: 2025/7/13
 */
@Repository
@Slf4j
public class UserFavoritesRepositoryImpl implements IUserFavoritesRepository {

    @Autowired
    private IUserFavoriteDao userFavoriteDao;

    @Autowired
    private UserFavoritesConverter converter;

    @Autowired
    private UserFavoritesCacheService cacheService;

    @Override
    public Optional<UserFavorites> findByUsername(Username username) {
        try {
            log.debug("查找用户收藏聚合根: {}", username.getValue());

            // 先尝试从缓存获取
            Optional<UserFavorites> cachedResult = cacheService.getUserFavorites(username);
            if (cachedResult.isPresent()) {
                log.debug("缓存命中 - 用户收藏: {}", username.getValue());
                return cachedResult;
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询用户收藏: {}", username.getValue());
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(username.getValue(), 0, Integer.MAX_VALUE);

            if (favorites.isEmpty()) {
                log.debug("用户收藏记录不存在: {}", username.getValue());
                return Optional.empty();
            }

            // 转换为聚合根
            UserFavorites userFavorites = converter.buildUserFavorites(username, favorites);

            // 缓存结果
            cacheService.cacheUserFavorites(userFavorites);
            log.debug("缓存用户收藏: {} - 收藏数: {}", username.getValue(), favorites.size());

            return Optional.of(userFavorites);

        } catch (Exception e) {
            log.error("查找用户收藏聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean saveIncremental(UserFavorites userFavorites) {
        try {
            if (!userFavorites.hasChanges()) {
                log.debug("用户收藏无变更，跳过保存: {}", userFavorites.getUsername().getValue());
                return true;
            }

            log.info("增量保存用户收藏: {} - {}", 
                    userFavorites.getUsername().getValue(), userFavorites.getChangesSummary());

            boolean success = true;

            // 处理收藏变更
            Set<ChangeTracker.ChangeRecord> favoriteChanges = userFavorites.getFavoriteChanges();
            for (ChangeTracker.ChangeRecord change : favoriteChanges) {
                success &= processFavoriteChange(userFavorites.getUsername(), change);
            }

            if (success) {
                // 清空变更记录
                userFavorites.clearChanges();
                
                // 更新缓存
                cacheService.cacheUserFavorites(userFavorites);
                
                // 清除相关缓存
                cacheService.evictRelatedCaches(userFavorites.getUsername());
                
                log.info("用户收藏增量保存成功: {}", userFavorites.getUsername().getValue());
            } else {
                log.error("用户收藏增量保存失败: {}", userFavorites.getUsername().getValue());
            }

            return success;

        } catch (Exception e) {
            log.error("用户收藏增量保存异常: {} - {}", 
                    userFavorites.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean save(UserFavorites userFavorites) {
        try {
            log.info("全量保存用户收藏: {} - 收藏数: {}", 
                    userFavorites.getUsername().getValue(), userFavorites.getFavoritedRelicsIds().size());

            // 先删除现有记录
            deleteByUsername(userFavorites.getUsername());

            // 批量插入新记录
            List<UserFavorite> favorites = converter.convertToUserFavorites(userFavorites);
            if (!favorites.isEmpty()) {
                int insertCount = userFavoriteDao.batchInsert(favorites);
                if (insertCount != favorites.size()) {
                    log.error("批量插入收藏记录数量不匹配: 期望={}, 实际={}", favorites.size(), insertCount);
                    return false;
                }
            }

            // 清空变更记录
            userFavorites.clearChanges();
            
            // 更新缓存
            cacheService.cacheUserFavorites(userFavorites);
            
            // 清除相关缓存
            cacheService.evictRelatedCaches(userFavorites.getUsername());

            log.info("用户收藏全量保存成功: {}", userFavorites.getUsername().getValue());
            return true;

        } catch (Exception e) {
            log.error("用户收藏全量保存失败: {} - {}", 
                    userFavorites.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteByUsername(Username username) {
        try {
            log.info("删除用户收藏: {}", username.getValue());

            // 逻辑删除所有收藏记录
            List<UserFavorite> favorites = userFavoriteDao.selectByUsername(username.getValue(), 0, Integer.MAX_VALUE);
            for (UserFavorite favorite : favorites) {
                if (favorite.getStatus() == 0) { // 只删除正常状态的记录
                    favorite.setStatus(1);
                    favorite.setUpdateTime(LocalDateTime.now());
                    userFavoriteDao.update(favorite);
                }
            }

            // 清除缓存
            cacheService.evictUserFavorites(username);
            cacheService.evictRelatedCaches(username);

            log.info("删除用户收藏成功: {}", username.getValue());
            return true;

        } catch (Exception e) {
            log.error("删除用户收藏失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsByUsername(Username username) {
        try {
            long count = userFavoriteDao.countByUsername(username.getValue());
            return count > 0;
        } catch (Exception e) {
            log.error("检查用户收藏存在性失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 处理收藏变更
     */
    private boolean processFavoriteChange(Username username, ChangeTracker.ChangeRecord change) {
        try {
            FavoriteAction favoriteAction = (FavoriteAction) change.getEntity();
            Long relicsId = (Long) change.getEntityId();

            switch (change.getType()) {
                case ADDED:
                    return addFavoriteRecord(username, favoriteAction);
                case DELETED:
                    return deleteFavoriteRecord(username, relicsId);
                case MODIFIED:
                    return updateFavoriteRecord(username, favoriteAction);
                default:
                    log.warn("未知的变更类型: {}", change.getType());
                    return false;
            }
        } catch (Exception e) {
            log.error("处理收藏变更失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 添加收藏记录
     */
    private boolean addFavoriteRecord(Username username, FavoriteAction favoriteAction) {
        UserFavorite userFavorite = converter.convertToUserFavorite(username, favoriteAction);
        int result = userFavoriteDao.insert(userFavorite);
        
        if (result > 0) {
            // 清除收藏状态缓存
            cacheService.evictFavoriteStatus(username, favoriteAction.getRelicsId());
            log.debug("添加收藏记录成功: {} - {}", username.getValue(), favoriteAction.getRelicsId());
            return true;
        } else {
            log.error("添加收藏记录失败: {} - {}", username.getValue(), favoriteAction.getRelicsId());
            return false;
        }
    }

    /**
     * 删除收藏记录
     */
    private boolean deleteFavoriteRecord(Username username, Long relicsId) {
        UserFavorite existing = userFavoriteDao.selectByUsernameAndRelicsId(username.getValue(), relicsId);
        if (existing != null && existing.getStatus() == 0) {
            existing.setStatus(1);
            existing.setUpdateTime(LocalDateTime.now());
            int result = userFavoriteDao.update(existing);
            
            if (result > 0) {
                // 清除收藏状态缓存
                cacheService.evictFavoriteStatus(username, relicsId);
                log.debug("删除收藏记录成功: {} - {}", username.getValue(), relicsId);
                return true;
            }
        }
        
        log.error("删除收藏记录失败: {} - {}", username.getValue(), relicsId);
        return false;
    }

    /**
     * 更新收藏记录
     */
    private boolean updateFavoriteRecord(Username username, FavoriteAction favoriteAction) {
        UserFavorite existing = userFavoriteDao.selectByUsernameAndRelicsId(username.getValue(), favoriteAction.getRelicsId());
        if (existing != null) {
            existing.setUpdateTime(LocalDateTime.now());
            existing.setStatus(favoriteAction.isDeleted() ? 1 : 0);
            int result = userFavoriteDao.update(existing);
            
            if (result > 0) {
                // 清除收藏状态缓存
                cacheService.evictFavoriteStatus(username, favoriteAction.getRelicsId());
                log.debug("更新收藏记录成功: {} - {}", username.getValue(), favoriteAction.getRelicsId());
                return true;
            }
        }
        
        log.error("更新收藏记录失败: {} - {}", username.getValue(), favoriteAction.getRelicsId());
        return false;
    }
}
