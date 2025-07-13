package com.ling.infrastructure.cache.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ling.domain.interaction.model.entity.UserInteraction;
import com.ling.domain.interaction.model.valobj.CommentAction;
import com.ling.domain.interaction.model.valobj.RelicsComment;
import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.config.UserInteractionCacheConfig;
import com.ling.infrastructure.cache.util.CacheKeyGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户交互缓存服务实现
 * @Author: LingRJ
 * @Description: 实现用户交互相关的缓存操作
 * @DateTime: 2025/7/13
 */
@Service
@Slf4j
public class UserInteractionCacheService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    // ==================== 聚合根缓存操作 ====================
    
    /**
     * 获取用户交互聚合根缓存
     */
    public Optional<UserInteraction> getUserInteraction(Username username) {
        try {
            String key = CacheKeyGenerator.userInteractionKey(username);
            RBucket<UserInteraction> bucket = redissonClient.getBucket(key);
            UserInteraction userInteraction = bucket.get();
            
            if (userInteraction != null) {
                log.debug("缓存命中 - 用户交互: {}", username.getValue());
                return Optional.of(userInteraction);
            } else {
                log.debug("缓存未命中 - 用户交互: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户交互缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户交互聚合根
     */
    public void cacheUserInteraction(UserInteraction userInteraction) {
        try {
            String key = CacheKeyGenerator.userInteractionKey(userInteraction.getUsername());
            RBucket<UserInteraction> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.USER_INTERACTION_TTL);
            bucket.set(userInteraction, ttl);
            
            log.debug("缓存用户交互: {} - TTL: {}分钟", 
                     userInteraction.getUsername().getValue(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户交互失败: {} - {}", 
                     userInteraction.getUsername().getValue(), e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户交互聚合根缓存
     */
    public void evictUserInteraction(Username username) {
        try {
            String key = CacheKeyGenerator.userInteractionKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除用户交互缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户交互缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
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
                log.debug("缓存命中 - 收藏状态: {} - {}: {}", 
                         username.getValue(), relicsId, status);
                return Optional.of(status);
            } else {
                log.debug("缓存未命中 - 收藏状态: {} - {}", username.getValue(), relicsId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取收藏状态缓存失败: {} - {} - {}", 
                     username.getValue(), relicsId, e.getMessage(), e);
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
            log.error("缓存收藏状态失败: {} - {} - {} - {}", 
                     username.getValue(), relicsId, isFavorited, e.getMessage(), e);
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
    
    // ==================== 收藏列表缓存操作 ====================
    
    /**
     * 获取用户收藏列表缓存
     */
    public Optional<List<Long>> getUserFavoritesList(Username username, int page, int size) {
        try {
            String key = CacheKeyGenerator.userFavoritesListKey(username, page, size);
            RBucket<List<Long>> bucket = redissonClient.getBucket(key);
            List<Long> favoriteIds = bucket.get();
            
            if (favoriteIds != null) {
                log.debug("缓存命中 - 用户收藏列表: {} - page:{}, size:{}, count:{}", 
                         username.getValue(), page, size, favoriteIds.size());
                return Optional.of(favoriteIds);
            } else {
                log.debug("缓存未命中 - 用户收藏列表: {} - page:{}, size:{}", 
                         username.getValue(), page, size);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户收藏列表缓存失败: {} - page:{}, size:{} - {}", 
                     username.getValue(), page, size, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存用户收藏列表
     */
    public void cacheUserFavoritesList(Username username, int page, int size, List<Long> favoriteIds) {
        try {
            String key = CacheKeyGenerator.userFavoritesListKey(username, page, size);
            RBucket<List<Long>> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.FAVORITES_LIST_TTL);
            bucket.set(favoriteIds, ttl);
            
            log.debug("缓存用户收藏列表: {} - page:{}, size:{}, count:{} - TTL: {}分钟", 
                     username.getValue(), page, size, favoriteIds.size(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户收藏列表失败: {} - page:{}, size:{} - {}", 
                     username.getValue(), page, size, e.getMessage(), e);
        }
    }
    
    /**
     * 删除用户收藏列表缓存
     */
    public void evictUserFavoritesList(Username username) {
        try {
            // 删除所有分页的收藏列表缓存
            String pattern = CacheKeyGenerator.userFavoritesListKey(username) + "*";
            redissonClient.getKeys().deleteByPattern(pattern);
            log.debug("删除用户收藏列表缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户收藏列表缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }
    
    // ==================== 统计数据缓存操作 ====================
    
    /**
     * 获取文物收藏数量缓存
     */
    public Optional<Long> getRelicsFavoriteCount(Long relicsId) {
        try {
            String key = CacheKeyGenerator.relicsFavoriteCountKey(relicsId);
            RBucket<Long> bucket = redissonClient.getBucket(key);
            Long count = bucket.get();
            
            if (count != null) {
                log.debug("缓存命中 - 文物收藏数量: {} - {}", relicsId, count);
                return Optional.of(count);
            } else {
                log.debug("缓存未命中 - 文物收藏数量: {}", relicsId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取文物收藏数量缓存失败: {} - {}", relicsId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * 缓存文物收藏数量
     */
    public void cacheRelicsFavoriteCount(Long relicsId, long count) {
        try {
            String key = CacheKeyGenerator.relicsFavoriteCountKey(relicsId);
            RBucket<Long> bucket = redissonClient.getBucket(key);
            
            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.COUNT_DATA_TTL);
            bucket.set(count, ttl);
            
            log.debug("缓存文物收藏数量: {} - {} - TTL: {}分钟", 
                     relicsId, count, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存文物收藏数量失败: {} - {} - {}", 
                     relicsId, count, e.getMessage(), e);
        }
    }
    
    /**
     * 删除文物收藏数量缓存
     */
    public void evictRelicsFavoriteCount(Long relicsId) {
        try {
            String key = CacheKeyGenerator.relicsFavoriteCountKey(relicsId);
            redissonClient.getBucket(key).delete();
            log.debug("删除文物收藏数量缓存: {}", relicsId);
        } catch (Exception e) {
            log.error("删除文物收藏数量缓存失败: {} - {}", relicsId, e.getMessage(), e);
        }
    }
    
    // ==================== 用户收藏总数缓存操作 ====================

    /**
     * 获取用户收藏总数缓存
     */
    public Optional<Long> getUserFavoriteCount(Username username) {
        try {
            String key = CacheKeyGenerator.userFavoriteCountKey(username);
            RBucket<Long> bucket = redissonClient.getBucket(key);
            Long count = bucket.get();

            if (count != null) {
                log.debug("缓存命中 - 用户收藏总数: {} - {}", username.getValue(), count);
                return Optional.of(count);
            } else {
                log.debug("缓存未命中 - 用户收藏总数: {}", username.getValue());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户收藏总数缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 缓存用户收藏总数
     */
    public void cacheUserFavoriteCount(Username username, long count) {
        try {
            String key = CacheKeyGenerator.userFavoriteCountKey(username);
            RBucket<Long> bucket = redissonClient.getBucket(key);

            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.COUNT_DATA_TTL);
            bucket.set(count, ttl);

            log.debug("缓存用户收藏总数: {} - {} - TTL: {}分钟",
                     username.getValue(), count, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户收藏总数失败: {} - {} - {}",
                     username.getValue(), count, e.getMessage(), e);
        }
    }

    /**
     * 删除用户收藏总数缓存
     */
    public void evictUserFavoriteCount(Username username) {
        try {
            String key = CacheKeyGenerator.userFavoriteCountKey(username);
            redissonClient.getBucket(key).delete();
            log.debug("删除用户收藏总数缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户收藏总数缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }

    // ==================== 评论缓存操作 ====================

    /**
     * 获取用户评论列表缓存
     */
    public Optional<List<CommentAction>> getUserComments(Username username, Long relicsId, int page, int size) {
        try {
            String key = CacheKeyGenerator.userCommentsKey(username, relicsId, page, size);
            RBucket<List<CommentAction>> bucket = redissonClient.getBucket(key);
            List<CommentAction> comments = bucket.get();

            if (comments != null) {
                log.debug("缓存命中 - 用户评论列表: {} - relicsId:{}, page:{}, size:{}, count:{}",
                         username.getValue(), relicsId, page, size, comments.size());
                return Optional.of(comments);
            } else {
                log.debug("缓存未命中 - 用户评论列表: {} - relicsId:{}, page:{}, size:{}",
                         username.getValue(), relicsId, page, size);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取用户评论列表缓存失败: {} - relicsId:{}, page:{}, size:{} - {}",
                     username.getValue(), relicsId, page, size, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 缓存用户评论列表
     */
    public void cacheUserComments(Username username, Long relicsId, int page, int size, List<CommentAction> comments) {
        try {
            String key = CacheKeyGenerator.userCommentsKey(username, relicsId, page, size);
            RBucket<List<CommentAction>> bucket = redissonClient.getBucket(key);

            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.COMMENTS_LIST_TTL);
            bucket.set(comments, ttl);

            log.debug("缓存用户评论列表: {} - relicsId:{}, page:{}, size:{}, count:{} - TTL: {}分钟",
                     username.getValue(), relicsId, page, size, comments.size(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存用户评论列表失败: {} - relicsId:{}, page:{}, size:{} - {}",
                     username.getValue(), relicsId, page, size, e.getMessage(), e);
        }
    }

    /**
     * 删除用户评论列表缓存
     */
    public void evictUserComments(Username username, Long relicsId) {
        try {
            String pattern;
            if (relicsId != null) {
                pattern = CacheKeyGenerator.userCommentsKey(username, relicsId) + "*";
            } else {
                pattern = CacheKeyGenerator.userCommentsKey(username, null) + "*";
            }
            redissonClient.getKeys().deleteByPattern(pattern);
            log.debug("删除用户评论列表缓存: {} - relicsId:{}", username.getValue(), relicsId);
        } catch (Exception e) {
            log.error("删除用户评论列表缓存失败: {} - relicsId:{} - {}",
                     username.getValue(), relicsId, e.getMessage(), e);
        }
    }

    /**
     * 获取文物已审核评论列表缓存
     */
    public Optional<List<RelicsComment>> getRelicsApprovedComments(Long relicsId, int page, int size) {
        try {
            String key = CacheKeyGenerator.relicsApprovedCommentsKey(relicsId, page, size);
            RBucket<List<RelicsComment>> bucket = redissonClient.getBucket(key);
            List<RelicsComment> comments = bucket.get();

            if (comments != null) {
                log.debug("缓存命中 - 文物已审核评论列表: {} - page:{}, size:{}, count:{}",
                         relicsId, page, size, comments.size());
                return Optional.of(comments);
            } else {
                log.debug("缓存未命中 - 文物已审核评论列表: {} - page:{}, size:{}",
                         relicsId, page, size);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取文物已审核评论列表缓存失败: {} - page:{}, size:{} - {}",
                     relicsId, page, size, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 缓存文物已审核评论列表
     */
    public void cacheRelicsApprovedComments(Long relicsId, int page, int size, List<RelicsComment> comments) {
        try {
            String key = CacheKeyGenerator.relicsApprovedCommentsKey(relicsId, page, size);
            RBucket<List<RelicsComment>> bucket = redissonClient.getBucket(key);

            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.COMMENTS_LIST_TTL);
            bucket.set(comments, ttl);

            log.debug("缓存文物已审核评论列表: {} - page:{}, size:{}, count:{} - TTL: {}分钟",
                     relicsId, page, size, comments.size(), ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存文物已审核评论列表失败: {} - page:{}, size:{} - {}",
                     relicsId, page, size, e.getMessage(), e);
        }
    }

    /**
     * 删除文物已审核评论列表缓存
     */
    public void evictRelicsApprovedComments(Long relicsId) {
        try {
            String pattern = CacheKeyGenerator.relicsApprovedCommentsKey(relicsId) + "*";
            redissonClient.getKeys().deleteByPattern(pattern);
            log.debug("删除文物已审核评论列表缓存: {}", relicsId);
        } catch (Exception e) {
            log.error("删除文物已审核评论列表缓存失败: {} - {}", relicsId, e.getMessage(), e);
        }
    }

    /**
     * 获取文物评论数量缓存
     */
    public Optional<Long> getRelicsCommentCount(Long relicsId) {
        try {
            String key = CacheKeyGenerator.relicsCommentCountKey(relicsId);
            RBucket<Long> bucket = redissonClient.getBucket(key);
            Long count = bucket.get();

            if (count != null) {
                log.debug("缓存命中 - 文物评论数量: {} - {}", relicsId, count);
                return Optional.of(count);
            } else {
                log.debug("缓存未命中 - 文物评论数量: {}", relicsId);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("获取文物评论数量缓存失败: {} - {}", relicsId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 缓存文物评论数量
     */
    public void cacheRelicsCommentCount(Long relicsId, long count) {
        try {
            String key = CacheKeyGenerator.relicsCommentCountKey(relicsId);
            RBucket<Long> bucket = redissonClient.getBucket(key);

            Duration ttl = UserInteractionCacheConfig.getRandomizedTtl(
                UserInteractionCacheConfig.COUNT_DATA_TTL);
            bucket.set(count, ttl);

            log.debug("缓存文物评论数量: {} - {} - TTL: {}分钟",
                     relicsId, count, ttl.toMinutes());
        } catch (Exception e) {
            log.error("缓存文物评论数量失败: {} - {} - {}",
                     relicsId, count, e.getMessage(), e);
        }
    }

    /**
     * 删除文物评论数量缓存
     */
    public void evictRelicsCommentCount(Long relicsId) {
        try {
            String key = CacheKeyGenerator.relicsCommentCountKey(relicsId);
            redissonClient.getBucket(key).delete();
            log.debug("删除文物评论数量缓存: {}", relicsId);
        } catch (Exception e) {
            log.error("删除文物评论数量缓存失败: {} - {}", relicsId, e.getMessage(), e);
        }
    }

    // ==================== 批量操作 ====================

    /**
     * 删除用户相关的所有缓存
     */
    public void evictUserRelatedCaches(Username username) {
        try {
            String pattern = CacheKeyGenerator.userRelatedKeysPattern(username);
            redissonClient.getKeys().deleteByPattern(pattern);
            log.info("删除用户相关所有缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("删除用户相关缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }

    /**
     * 删除文物相关的所有缓存
     */
    public void evictRelicsRelatedCaches(Long relicsId) {
        try {
            String pattern = CacheKeyGenerator.relicsRelatedKeysPattern(relicsId);
            redissonClient.getKeys().deleteByPattern(pattern);
            log.info("删除文物相关所有缓存: {}", relicsId);
        } catch (Exception e) {
            log.error("删除文物相关缓存失败: {} - {}", relicsId, e.getMessage(), e);
        }
    }

    /**
     * 预热用户交互缓存
     */
    public void warmUpUserCache(Username username) {
        try {
            // 这里可以实现缓存预热逻辑
            // 例如：预加载用户的基本交互数据
            log.debug("预热用户缓存: {}", username.getValue());
        } catch (Exception e) {
            log.error("预热用户缓存失败: {} - {}", username.getValue(), e.getMessage(), e);
        }
    }

    // ==================== 分布式锁操作 ====================

    /**
     * 获取分布式锁
     */
    public RLock getLock(String businessKey) {
        String lockKey = CacheKeyGenerator.lockKey(businessKey);
        return redissonClient.getLock(lockKey);
    }

    /**
     * 尝试获取锁并执行操作
     */
    public <T> T executeWithLock(String businessKey, java.util.function.Supplier<T> operation) {
        RLock lock = getLock(businessKey);
        try {
            boolean acquired = lock.tryLock(
                UserInteractionCacheConfig.LOCK_WAIT_SECONDS,
                UserInteractionCacheConfig.LOCK_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
            );

            if (acquired) {
                try {
                    return operation.get();
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("获取分布式锁失败: {}", businessKey);
                return operation.get(); // 降级执行
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: {} - {}", businessKey, e.getMessage());
            return operation.get(); // 降级执行
        } catch (Exception e) {
            log.error("分布式锁操作失败: {} - {}", businessKey, e.getMessage(), e);
            return operation.get(); // 降级执行
        }
    }
}
