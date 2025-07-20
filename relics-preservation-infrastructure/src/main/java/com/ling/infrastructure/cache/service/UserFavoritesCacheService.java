package com.ling.infrastructure.cache.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ling.domain.interaction.model.entity.UserFavorites;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.config.UserInteractionCacheConfig;
import com.ling.infrastructure.cache.util.CacheKeyGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户收藏缓存服务
 * @Author: LingRJ
 * @Description: 实现用户收藏聚合根的缓存操作
 * @DateTime: 2025/7/13
 */
@Service
@Slf4j
public class UserFavoritesCacheService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    // ==================== 聚合根缓存操作 ====================
    
    /**
     * 获取用户收藏聚合根缓存
     */
    public Optional<UserFavorites> getUserFavorites(Username username) {
        try {
            String key = CacheKeyGenerator.userFavoritesKey(username);
            RBucket<UserFavorites> bucket = redissonClient.getBucket(key);
            UserFavorites userFavorites = bucket.get();
            
            if (userFavorites != null) {
                log.debug("缓存命中 - 用户收藏: {}", username.getValue());
                return Optional.of(userFavorites);
            } else {
                log.debug("缓存未命中 - 用户收藏: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户收藏缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户收藏聚合根
     */
    public void cacheUserFavorites(UserFavorites userFavorites) {
        try {
            String key = CacheKeyGenerator.userFavoritesKey(userFavorites.getUsername());
            RBucket<UserFavorites> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.USER_FAVORITES_TTL);
            bucket.set(userFavorites, ttl);
            
            log.debug("缓存用户收藏: {} - TTL: {}分钟", 
                     userFavorites.getUsername().getValue(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户收藏失败: {} - {}", 
                     userFavorites.getUsername().getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户收藏聚合根缓存
     */
    public void evictUserFavorites(Username username) {
        try {
            String key = CacheKeyGenerator.userFavoritesKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除用户收藏缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户收藏缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 收藏状态缓存操作 ====================
    
    /**
     * 获取用户收藏状态缓存
     */
    public Optional<Boolean> getFavoriteStatus(Username username, Long relicsId) {
        try {
            String key = CacheKeyGenerator.userFavoriteStatusKey(username, relicsId);
            RBucket<Boolean> bucket = redissonClient.getBucket(key);
            Boolean status = bucket.get();
            
            if (status != null) {
                log.debug("缓存命中 - 收藏状态: {} - {}: {}", username.getValue(), relicsId, status);
                return Optional.of(status);
            } else {
                log.debug("缓存未命中 - 收藏状态: {} - {}", username.getValue(), relicsId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取收藏状态缓存失败: {} - {} - {}", username.getValue(), relicsId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户收藏状态
     */
    public void cacheFavoriteStatus(Username username, Long relicsId, boolean isFavorited) {
        try {
            String key = CacheKeyGenerator.userFavoriteStatusKey(username, relicsId);
            RBucket<Boolean> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.FAVORITE_STATUS_TTL);
            bucket.set(isFavorited, ttl);
            
            log.debug("缓存收藏状态: {} - {}: {} - TTL: {}分钟", 
                     username.getValue(), relicsId, isFavorited, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存收藏状态失败: {} - {} - {}", 
                     username.getValue(), relicsId, e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户收藏状态缓存
     */
    public void evictFavoriteStatus(Username username, Long relicsId) {
        try {
            String key = CacheKeyGenerator.userFavoriteStatusKey(username, relicsId);
            redissonClient.getBucket(key).delete();
            log.debug("删除收藏状态缓存: {} - {}", username.getValue(), relicsId);
        } catch (Exception e) {
            log.error("删除收藏状态缓存失败: {} - {} - {}", 
                     username.getValue(), relicsId, e.getMessage(), e);
        }
    }
    
    // ==================== 收藏统计缓存操作 ====================
    
    /**
     * 获取用户收藏统计缓存
     */
    public Optional<UserFavorites.FavoriteStatistics> getFavoriteStatistics(Username username) {
        try {
            String key = CacheKeyGenerator.userFavoriteStatisticsKey(username);
            RBucket<UserFavorites.FavoriteStatistics> bucket = redissonClient.getBucket(key);
            UserFavorites.FavoriteStatistics stats = bucket.get();
            
            if (stats != null) {
                log.debug("缓存命中 - 收藏统计: {}", username.getValue());
                return Optional.of(stats);
            } else {
                log.debug("缓存未命中 - 收藏统计: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取收藏统计缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户收藏统计
     */
    public void cacheFavoriteStatistics(UserFavorites.FavoriteStatistics statistics) {
        try {
            String key = CacheKeyGenerator.userFavoriteStatisticsKey(Username.of(statistics.getUsername()));
            RBucket<UserFavorites.FavoriteStatistics> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.FAVORITE_STATISTICS_TTL);
            bucket.set(statistics, ttl);
            
            log.debug("缓存收藏统计: {} - TTL: {}分钟", 
                     statistics.getUsername(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存收藏统计失败: {} - {}", 
                     statistics.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户收藏统计缓存
     */
    public void evictFavoriteStatistics(Username username) {
        try {
            String key = CacheKeyGenerator.userFavoriteStatisticsKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除收藏统计缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除收藏统计缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 批量缓存操作 ====================
    
    /**
     * 清除用户相关的所有缓存
     */
    public void evictRelatedCaches(Username username) {
        try {
            log.debug("清除用户相关缓存: {}", username.getValue());
            
            // 清除收藏状态缓存（通过模式匹配）
            String pattern = CacheKeyGenerator.userFavoriteStatusPattern(username);
            redissonClient.getKeys().deleteByPattern(pattern);
            
            // 清除收藏统计缓存
            evictFavoriteStatistics(username);
            
            log.debug("清除用户相关缓存完成: {}", username.getValue());
        } catch (Exception e) {
            log.error("清除用户相关缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 使用分布式锁执行缓存操作
     */
    public <T> T executeWithLock(Username username, String operation, java.util.function.Supplier<T> supplier) {
        String lockKey = CacheKeyGenerator.userFavoritesLockKey(username, operation);
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
}
