package com.ling.infrastructure.cache.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ling.domain.interaction.model.entity.GalleryManager;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.config.UserInteractionCacheConfig;
import com.ling.infrastructure.cache.util.CacheKeyGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * 收藏馆管理缓存服务
 * @Author: LingRJ
 * @Description: 实现收藏馆管理聚合根的缓存操作
 * @DateTime: 2025/7/13
 */
@Service
@Slf4j
public class GalleryManagerCacheService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    // ==================== 聚合根缓存操作 ====================
    
    /**
     * 获取收藏馆管理聚合根缓存
     */
    public Optional<GalleryManager> getGalleryManager(Username username) {
        try {
            String key = CacheKeyGenerator.galleryManagerKey(username);
            RBucket<GalleryManager> bucket = redissonClient.getBucket(key);
            GalleryManager galleryManager = bucket.get();
            
            if (galleryManager != null) {
                log.debug("缓存命中 - 收藏馆管理: {}", username.getValue());
                return Optional.of(galleryManager);
            } else {
                log.debug("缓存未命中 - 收藏馆管理: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取收藏馆管理缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存收藏馆管理聚合根
     */
    public void cacheGalleryManager(GalleryManager galleryManager) {
        try {
            String key = CacheKeyGenerator.galleryManagerKey(galleryManager.getUsername());
            RBucket<GalleryManager> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.GALLERY_MANAGER_TTL);
            bucket.set(galleryManager, ttl);
            
            log.debug("缓存收藏馆管理: {} - TTL: {}分钟", 
                     galleryManager.getUsername().getValue(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存收藏馆管理失败: {} - {}", 
                     galleryManager.getUsername().getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除收藏馆管理聚合根缓存
     */
    public void evictGalleryManager(Username username) {
        try {
            String key = CacheKeyGenerator.galleryManagerKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除收藏馆管理缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除收藏馆管理缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 收藏馆统计缓存操作 ====================
    
    /**
     * 获取收藏馆统计缓存
     */
    public Optional<GalleryManager.GalleryStatistics> getGalleryStatistics(Username username) {
        try {
            String key = CacheKeyGenerator.galleryStatisticsKey(username);
            RBucket<GalleryManager.GalleryStatistics> bucket = redissonClient.getBucket(key);
            GalleryManager.GalleryStatistics stats = bucket.get();
            
            if (stats != null) {
                log.debug("缓存命中 - 收藏馆统计: {}", username.getValue());
                return Optional.of(stats);
            } else {
                log.debug("缓存未命中 - 收藏馆统计: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取收藏馆统计缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存收藏馆统计
     */
    public void cacheGalleryStatistics(GalleryManager.GalleryStatistics statistics) {
        try {
            String key = CacheKeyGenerator.galleryStatisticsKey(Username.of(statistics.getUsername()));
            RBucket<GalleryManager.GalleryStatistics> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.GALLERY_STATISTICS_TTL);
            bucket.set(statistics, ttl);
            
            log.debug("缓存收藏馆统计: {} - TTL: {}分钟", 
                     statistics.getUsername(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存收藏馆统计失败: {} - {}", 
                     statistics.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除收藏馆统计缓存
     */
    public void evictGalleryStatistics(Username username) {
        try {
            String key = CacheKeyGenerator.galleryStatisticsKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除收藏馆统计缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除收藏馆统计缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 收藏馆详情缓存操作 ====================
    
    /**
     * 获取收藏馆详情缓存
     */
    public Optional<Object> getGalleryDetail(String galleryId) {
        try {
            String key = CacheKeyGenerator.galleryDetailKey(galleryId);
            RBucket<Object> bucket = redissonClient.getBucket(key);
            Object gallery = bucket.get();
            
            if (gallery != null) {
                log.debug("缓存命中 - 收藏馆详情: {}", galleryId);
                return Optional.of(gallery);
            } else {
                log.debug("缓存未命中 - 收藏馆详情: {}", galleryId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取收藏馆详情缓存失败: {} - {}", galleryId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存收藏馆详情
     */
    public void cacheGalleryDetail(String galleryId, Object gallery) {
        try {
            String key = CacheKeyGenerator.galleryDetailKey(galleryId);
            RBucket<Object> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.GALLERY_MANAGER_TTL);
            bucket.set(gallery, ttl);
            
            log.debug("缓存收藏馆详情: {} - TTL: {}分钟", galleryId, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存收藏馆详情失败: {} - {}", galleryId, e.getMessage(), e);
        }
    }
    
    /**
     * 删除收藏馆详情缓存
     */
    public void evictGalleryDetailCache(String galleryId) {
        try {
            String key = CacheKeyGenerator.galleryDetailKey(galleryId);
            redissonClient.getBucket(key).delete();
            log.debug("删除收藏馆详情缓存: {}", galleryId);
        } catch (Exception e) {
            log.error("删除收藏馆详情缓存失败: {} - {}", galleryId, e.getMessage(), e);
        }
    }
    
    // ==================== 收藏馆列表缓存操作 ====================
    
    /**
     * 获取用户收藏馆列表缓存
     */
    public Optional<Object> getUserGalleryList(Username username, int page, int size) {
        try {
            String key = CacheKeyGenerator.userGalleryListKey(username, page, size);
            RBucket<Object> bucket = redissonClient.getBucket(key);
            Object galleries = bucket.get();
            
            if (galleries != null) {
                log.debug("缓存命中 - 用户收藏馆列表: {} - 页码: {}", username.getValue(), page);
                return Optional.of(galleries);
            } else {
                log.debug("缓存未命中 - 用户收藏馆列表: {} - 页码: {}", username.getValue(), page);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户收藏馆列表缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户收藏馆列表
     */
    public void cacheUserGalleryList(Username username, int page, int size, Object galleries) {
        try {
            String key = CacheKeyGenerator.userGalleryListKey(username, page, size);
            RBucket<Object> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.GALLERY_MANAGER_TTL);
            bucket.set(galleries, ttl);
            
            log.debug("缓存用户收藏馆列表: {} - 页码: {} - TTL: {}分钟", 
                     username.getValue(), page, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户收藏馆列表失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户收藏馆列表缓存
     */
    public void evictGalleryListCaches(Username username) {
        try {
            String pattern = CacheKeyGenerator.userGalleryListPattern(username);
            redissonClient.getKeys().deleteByPattern(pattern);
            log.debug("删除用户收藏馆列表缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户收藏馆列表缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 批量缓存操作 ====================
    
    /**
     * 清除用户相关的所有缓存
     */
    public void evictRelatedCaches(Username username) {
        try {
            log.debug("清除用户收藏馆相关缓存: {}", username.getValue());
            
            // 清除收藏馆统计缓存
            evictGalleryStatistics(username);
            
            // 清除收藏馆列表缓存
            evictGalleryListCaches(username);
            
            log.debug("清除用户收藏馆相关缓存完成: {}", username.getValue());
        } catch (Exception e) {
            log.error("清除用户收藏馆相关缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 预热收藏馆缓存
     */
    public void warmupGalleryManagerCache(Username username, GalleryManager galleryManager) {
        try {
            log.debug("预热收藏馆管理缓存: {}", username.getValue());
            
            // 缓存聚合根
            cacheGalleryManager(galleryManager);
            
            // 缓存统计信息
            GalleryManager.GalleryStatistics stats = galleryManager.getStatistics();
            cacheGalleryStatistics(stats);
            
            log.debug("预热收藏馆管理缓存完成: {}", username.getValue());
        } catch (Exception e) {
            log.error("预热收藏馆管理缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 使用分布式锁执行缓存操作
     */
    public <T> T executeWithLock(Username username, String operation, java.util.function.Supplier<T> supplier) {
        String lockKey = CacheKeyGenerator.galleryManagerLockKey(username, operation);
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                try {
                    return supplier.get();
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("获取分布式锁失败: {} - {}", username.getValue(), operation);
                throw new RuntimeException("获取分布式锁失败");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("分布式锁操作被中断: {} - {}", username.getValue(), operation);
            throw new RuntimeException("分布式锁操作被中断", e);
        } catch (Exception e) {
            log.error("分布式锁操作失败: {} - {} - {}", username.getValue(), operation, e.getMessage(), e);
            throw new RuntimeException("分布式锁操作失败", e);
        }
    }
    
    /**
     * 检查缓存健康状态
     */
    public boolean isHealthy() {
        try {
            // 简单的健康检查：尝试执行一个基本操作
            String testKey = "health:check:galleries:" + System.currentTimeMillis();
            RBucket<String> bucket = redissonClient.getBucket(testKey);
            bucket.set("test", Duration.ofSeconds(1));
            String result = bucket.get();
            bucket.delete();
            
            return "test".equals(result);
        } catch (Exception e) {
            log.error("收藏馆缓存健康检查失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
