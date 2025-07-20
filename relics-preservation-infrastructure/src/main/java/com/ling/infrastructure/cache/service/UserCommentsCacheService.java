package com.ling.infrastructure.cache.service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ling.domain.interaction.model.entity.UserComments;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.config.UserInteractionCacheConfig;
import com.ling.infrastructure.cache.util.CacheKeyGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户评论缓存服务
 * @Author: LingRJ
 * @Description: 实现用户评论聚合根的缓存操作
 * @DateTime: 2025/7/13
 */
@Service
@Slf4j
public class UserCommentsCacheService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    // ==================== 聚合根缓存操作 ====================
    
    /**
     * 获取用户评论聚合根缓存
     */
    public Optional<UserComments> getUserComments(Username username) {
        try {
            String key = CacheKeyGenerator.userCommentsKey(username);
            RBucket<UserComments> bucket = redissonClient.getBucket(key);
            UserComments userComments = bucket.get();
            
            if (userComments != null) {
                log.debug("缓存命中 - 用户评论: {}", username.getValue());
                return Optional.of(userComments);
            } else {
                log.debug("缓存未命中 - 用户评论: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户评论缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户评论聚合根
     */
    public void cacheUserComments(UserComments userComments) {
        try {
            String key = CacheKeyGenerator.userCommentsKey(userComments.getUsername());
            RBucket<UserComments> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.USER_COMMENTS_TTL);
            bucket.set(userComments, ttl);
            
            log.debug("缓存用户评论: {} - TTL: {}分钟", 
                     userComments.getUsername().getValue(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户评论失败: {} - {}", 
                     userComments.getUsername().getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户评论聚合根缓存
     */
    public void evictUserComments(Username username) {
        try {
            String key = CacheKeyGenerator.userCommentsKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除用户评论缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户评论缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 评论统计缓存操作 ====================
    
    /**
     * 获取用户评论统计缓存
     */
    public Optional<UserComments.CommentStatistics> getCommentStatistics(Username username) {
        try {
            String key = CacheKeyGenerator.userCommentStatisticsKey(username);
            RBucket<UserComments.CommentStatistics> bucket = redissonClient.getBucket(key);
            UserComments.CommentStatistics stats = bucket.get();
            
            if (stats != null) {
                log.debug("缓存命中 - 评论统计: {}", username.getValue());
                return Optional.of(stats);
            } else {
                log.debug("缓存未命中 - 评论统计: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取评论统计缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户评论统计
     */
    public void cacheCommentStatistics(UserComments.CommentStatistics statistics) {
        try {
            String key = CacheKeyGenerator.userCommentStatisticsKey(Username.of(statistics.getUsername()));
            RBucket<UserComments.CommentStatistics> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.COMMENT_STATISTICS_TTL);
            bucket.set(statistics, ttl);
            
            log.debug("缓存评论统计: {} - TTL: {}分钟", 
                     statistics.getUsername(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存评论统计失败: {} - {}", 
                     statistics.getUsername(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户评论统计缓存
     */
    public void evictCommentStatistics(Username username) {
        try {
            String key = CacheKeyGenerator.userCommentStatisticsKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除评论统计缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除评论统计缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 文物评论缓存操作 ====================
    
    /**
     * 获取文物评论列表缓存
     */
    public Optional<Object> getRelicsComments(Long relicsId, int page, int size) {
        try {
            String key = CacheKeyGenerator.relicsCommentsKey(relicsId, page, size);
            RBucket<Object> bucket = redissonClient.getBucket(key);
            Object comments = bucket.get();
            
            if (comments != null) {
                log.debug("缓存命中 - 文物评论: {} - 页码: {}", relicsId, page);
                return Optional.of(comments);
            } else {
                log.debug("缓存未命中 - 文物评论: {} - 页码: {}", relicsId, page);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取文物评论缓存失败: {} - {}", relicsId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存文物评论列表
     */
    public void cacheRelicsComments(Long relicsId, int page, int size, Object comments) {
        try {
            String key = CacheKeyGenerator.relicsCommentsKey(relicsId, page, size);
            RBucket<Object> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.RELICS_COMMENTS_TTL);
            bucket.set(comments, ttl);
            
            log.debug("缓存文物评论: {} - 页码: {} - TTL: {}分钟", 
                     relicsId, page, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存文物评论失败: {} - {}", relicsId, e.getMessage(), e);
        }
    }
    
    /**
     * 删除文物相关的评论缓存
     */
    public void evictRelicsCommentCaches(Long relicsId) {
        try {
            String pattern = CacheKeyGenerator.relicsCommentsPattern(relicsId);
            redissonClient.getKeys().deleteByPattern(pattern);
            log.debug("删除文物评论缓存: {}", relicsId);
        } catch (Exception e) {
            log.error("删除文物评论缓存失败: {} - {}", relicsId, e.getMessage(), e);
        }
    }
    
    // ==================== 批量缓存操作 ====================
    
    /**
     * 清除用户相关的所有缓存
     */
    public void evictRelatedCaches(Username username) {
        try {
            log.debug("清除用户评论相关缓存: {}", username.getValue());
            
            // 清除评论统计缓存
            evictCommentStatistics(username);
            
            // 清除用户评论列表缓存（通过模式匹配）
            String pattern = CacheKeyGenerator.userCommentsPattern(username);
            redissonClient.getKeys().deleteByPattern(pattern);
            
            log.debug("清除用户评论相关缓存完成: {}", username.getValue());
        } catch (Exception e) {
            log.error("清除用户评论相关缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 预热用户评论缓存
     */
    public void warmupUserCommentsCache(Username username, UserComments userComments) {
        try {
            log.debug("预热用户评论缓存: {}", username.getValue());
            
            // 缓存聚合根
            cacheUserComments(userComments);
            
            // 缓存统计信息
            UserComments.CommentStatistics stats = userComments.getStatistics();
            cacheCommentStatistics(stats);
            
            log.debug("预热用户评论缓存完成: {}", username.getValue());
        } catch (Exception e) {
            log.error("预热用户评论缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 使用分布式锁执行缓存操作
     */
    public <T> T executeWithLock(Username username, String operation, java.util.function.Supplier<T> supplier) {
        String lockKey = CacheKeyGenerator.userCommentsLockKey(username, operation);
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
            String testKey = "health:check:comments:" + System.currentTimeMillis();
            RBucket<String> bucket = redissonClient.getBucket(testKey);
            bucket.set("test", Duration.ofSeconds(1));
            String result = bucket.get();
            bucket.delete();
            
            return "test".equals(result);
        } catch (Exception e) {
            log.error("评论缓存健康检查失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
