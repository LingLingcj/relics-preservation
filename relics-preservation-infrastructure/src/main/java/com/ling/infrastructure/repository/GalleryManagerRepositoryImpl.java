package com.ling.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ling.domain.interaction.adapter.IGalleryManagerRepository;
import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.interaction.model.valobj.ChangeTracker;
import com.ling.domain.interaction.model.valobj.CollectionGallery;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.service.GalleryManagerCacheService;
import com.ling.infrastructure.dao.ICollectionGalleryDao;
import com.ling.infrastructure.dao.po.CollectionGalleryPO;
import com.ling.infrastructure.repository.converter.GalleryManagerConverter;

import lombok.extern.slf4j.Slf4j;

/**
 * 收藏馆管理仓储实现
 * @Author: LingRJ
 * @Description: 实现收藏馆管理聚合根的数据访问逻辑
 * @DateTime: 2025/7/13
 */
@Repository
@Slf4j
public class GalleryManagerRepositoryImpl implements IGalleryManagerRepository {

    @Autowired
    private ICollectionGalleryDao collectionGalleryDao;

    @Autowired
    private GalleryManagerConverter converter;

    @Autowired
    private GalleryManagerCacheService cacheService;

    @Override
    public Optional<GalleryManager> findByUsername(Username username) {
        try {
            log.debug("查找收藏馆管理聚合根: {}", username.getValue());

            // 先尝试从缓存获取
            Optional<GalleryManager> cachedResult = cacheService.getGalleryManager(username);
            if (cachedResult.isPresent()) {
                log.debug("缓存命中 - 收藏馆管理: {}", username.getValue());
                return cachedResult;
            }

            // 缓存未命中，从数据库查询
            log.debug("缓存未命中，从数据库查询收藏馆管理: {}", username.getValue());
            List<CollectionGalleryPO> galleries = collectionGalleryDao.selectByUsername(username.getValue());

            // 过滤有效的收藏馆记录
            galleries = converter.filterValidGalleryPOs(galleries);

            if (galleries.isEmpty()) {
                log.debug("收藏馆管理记录不存在: {}", username.getValue());
                return Optional.empty();
            }

            // 转换为聚合根
            GalleryManager galleryManager = converter.buildGalleryManager(username, galleries);

            // 缓存结果
            cacheService.cacheGalleryManager(galleryManager);
            log.debug("缓存收藏馆管理: {} - 收藏馆数: {}", username.getValue(), galleries.size());

            return Optional.of(galleryManager);

        } catch (Exception e) {
            log.error("查找收藏馆管理聚合根失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public boolean saveIncremental(GalleryManager galleryManager) {
        try {
            if (!galleryManager.hasChanges()) {
                log.debug("收藏馆管理无变更，跳过保存: {}", galleryManager.getUsername().getValue());
                return true;
            }

            log.info("增量保存收藏馆管理: {} - {}",
                    galleryManager.getUsername().getValue(), galleryManager.getChangesSummary());

            boolean success = true;

            // 处理收藏馆变更
            Set<ChangeTracker.ChangeRecord> galleryChanges = galleryManager.getGalleryChanges();
            for (ChangeTracker.ChangeRecord change : galleryChanges) {
                success &= processGalleryChange(galleryManager.getUsername(), change);
            }

            if (success) {
                // 清空变更记录
                galleryManager.clearChanges();

                // 更新缓存
                cacheService.cacheGalleryManager(galleryManager);

                // 清除相关缓存
                cacheService.evictRelatedCaches(galleryManager.getUsername());

                log.info("收藏馆管理增量保存成功: {}", galleryManager.getUsername().getValue());
            } else {
                log.error("收藏馆管理增量保存失败: {}", galleryManager.getUsername().getValue());
            }

            return success;

        } catch (Exception e) {
            log.error("收藏馆管理增量保存异常: {} - {}",
                    galleryManager.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean save(GalleryManager galleryManager) {
        try {
            log.info("全量保存收藏馆管理: {} - 收藏馆数: {}",
                    galleryManager.getUsername().getValue(), galleryManager.getAllGalleries().size());

            // 先删除现有记录
            deleteByUsername(galleryManager.getUsername());

            // 批量插入新记录
            List<CollectionGalleryPO> galleries = converter.convertToCollectionGalleries(galleryManager);
            if (!galleries.isEmpty()) {
                int insertCount = collectionGalleryDao.batchInsert(galleries);
                if (insertCount != galleries.size()) {
                    log.error("批量插入收藏馆记录数量不匹配: 期望={}, 实际={}", galleries.size(), insertCount);
                    return false;
                }
            }

            // 清空变更记录
            galleryManager.clearChanges();

            // 更新缓存
            cacheService.cacheGalleryManager(galleryManager);

            // 清除相关缓存
            cacheService.evictRelatedCaches(galleryManager.getUsername());

            log.info("收藏馆管理全量保存成功: {}", galleryManager.getUsername().getValue());
            return true;

        } catch (Exception e) {
            log.error("收藏馆管理全量保存失败: {} - {}",
                    galleryManager.getUsername().getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deleteByUsername(Username username) {
        try {
            log.info("删除收藏馆管理: {}", username.getValue());

            // 逻辑删除所有收藏馆记录
            List<CollectionGalleryPO> galleries = collectionGalleryDao.selectByUsername(username.getValue());
            for (CollectionGalleryPO gallery : galleries) {
                if (gallery.getStatus() == 0) { // 只删除正常状态的记录
                    gallery.setStatus(1);
                    gallery.setUpdateTime(LocalDateTime.now());
                    collectionGalleryDao.update(gallery);
                }
            }

            // 清除缓存
            cacheService.evictGalleryManager(username);
            cacheService.evictRelatedCaches(username);

            log.info("删除收藏馆管理成功（仅缓存）: {}", username.getValue());
            return true;

        } catch (Exception e) {
            log.error("删除收藏馆管理失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean existsByUsername(Username username) {
        try {
            long count = collectionGalleryDao.countByUsername(username.getValue());
            return count > 0;
        } catch (Exception e) {
            log.error("检查收藏馆管理存在性失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 处理收藏馆变更
     */
    private boolean processGalleryChange(Username username, ChangeTracker.ChangeRecord change) {
        try {
            CollectionGallery gallery = (CollectionGallery) change.getEntity();
            String galleryId = (String) change.getEntityId();

            switch (change.getType()) {
                case ADDED:
                    return addGalleryRecord(username, gallery);
                case DELETED:
                    return deleteGalleryRecord(username, galleryId);
                case MODIFIED:
                    return updateGalleryRecord(username, gallery);
                default:
                    log.warn("未知的变更类型: {}", change.getType());
                    return false;
            }
        } catch (Exception e) {
            log.error("处理收藏馆变更失败: {} - {}", username.getValue(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 添加收藏馆记录
     */
    private boolean addGalleryRecord(Username username, CollectionGallery gallery) {
        CollectionGalleryPO galleryPO = converter.convertToCollectionGalleryPO(username, gallery);
        int result = collectionGalleryDao.insert(galleryPO);

        if (result > 0) {
            // 清除相关缓存
            cacheService.evictGalleryListCaches(username);
            log.debug("添加收藏馆记录成功: {} - {}", username.getValue(), gallery.getName());
            return true;
        } else {
            log.error("添加收藏馆记录失败: {} - {}", username.getValue(), gallery.getName());
            return false;
        }
    }

    /**
     * 删除收藏馆记录
     */
    private boolean deleteGalleryRecord(Username username, String galleryId) {
        CollectionGalleryPO existing = collectionGalleryDao.selectByGalleryId(galleryId);
        if (existing != null && existing.getStatus() == 0) {
            existing.setStatus(1);
            existing.setUpdateTime(LocalDateTime.now());
            int result = collectionGalleryDao.update(existing);

            if (result > 0) {
                // 清除相关缓存
                cacheService.evictGalleryListCaches(username);
                cacheService.evictGalleryDetailCache(galleryId);
                log.debug("删除收藏馆记录成功: {} - {}", username.getValue(), galleryId);
                return true;
            }
        }

        log.error("删除收藏馆记录失败: {} - {}", username.getValue(), galleryId);
        return false;
    }

    /**
     * 更新收藏馆记录
     */
    private boolean updateGalleryRecord(Username username, CollectionGallery gallery) {
        CollectionGalleryPO existing = collectionGalleryDao.selectByGalleryId(gallery.getGalleryId().getValue());
        if (existing != null) {
            // 更新收藏馆信息
            existing.setName(gallery.getName());
            existing.setDescription(gallery.getDescription());
            existing.setTheme(gallery.getTheme().getCode());
            existing.setDisplayStyle(gallery.getDisplayStyle().getCode());
            existing.setIsPublic(gallery.isPublic() ? 1 : 0);
            existing.setCustomThemeName(gallery.getCustomThemeName());
            existing.setRelicsIds(String.join(",", gallery.getRelicsIds().stream().map(String::valueOf).toList()));
            existing.setUpdateTime(LocalDateTime.now());

            int result = collectionGalleryDao.update(existing);

            if (result > 0) {
                // 清除相关缓存
                cacheService.evictGalleryListCaches(username);
                cacheService.evictGalleryDetailCache(gallery.getGalleryId().getValue());
                log.debug("更新收藏馆记录成功: {} - {}", username.getValue(), gallery.getName());
                return true;
            }
        }

        log.error("更新收藏馆记录失败: {} - {}", username.getValue(), gallery.getName());
        return false;
    }
}
