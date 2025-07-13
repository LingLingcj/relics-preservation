package com.ling.infrastructure.cache.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 用户交互缓存配置常量
 * @Author: LingRJ
 * @Description: 定义用户交互相关的缓存键模板、过期时间等配置
 * @DateTime: 2025/7/13
 */
public final class UserInteractionCacheConfig {
    
    // ==================== 缓存键模板 ====================
    
    /**
     * 用户交互聚合根缓存键模板
     * 格式：user:interaction:{username}
     */
    public static final String USER_INTERACTION_KEY = "user:interaction:%s";
    
    /**
     * 用户收藏状态缓存键模板
     * 格式：user:favorite:{username}:{relicsId}
     */
    public static final String USER_FAVORITE_STATUS_KEY = "user:favorite:%s:%d";
    
    /**
     * 用户收藏列表缓存键模板
     * 格式：user:favorites:{username}
     */
    public static final String USER_FAVORITES_LIST_KEY = "user:favorites:%s";
    
    /**
     * 文物收藏数量缓存键模板
     * 格式：relics:favorite:count:{relicsId}
     */
    public static final String RELICS_FAVORITE_COUNT_KEY = "relics:favorite:count:%d";
    
    /**
     * 用户评论列表缓存键模板
     * 格式：user:comments:{username}:{relicsId}（relicsId可选）
     */
    public static final String USER_COMMENTS_KEY = "user:comments:%s:%s";
    
    /**
     * 文物评论数量缓存键模板
     * 格式：relics:comment:count:{relicsId}
     */
    public static final String RELICS_COMMENT_COUNT_KEY = "relics:comment:count:%d";
    
    /**
     * 文物已审核评论列表缓存键模板
     * 格式：relics:comments:approved:{relicsId}
     */
    public static final String RELICS_APPROVED_COMMENTS_KEY = "relics:comments:approved:%d";
    
    /**
     * 用户收藏总数缓存键模板
     * 格式：user:favorite:count:{username}
     */
    public static final String USER_FAVORITE_COUNT_KEY = "user:favorite:count:%s";
    
    /**
     * 用户评论总数缓存键模板
     * 格式：user:comment:count:{username}:{relicsId}
     */
    public static final String USER_COMMENT_COUNT_KEY = "user:comment:count:%s:%s";
    
    // ==================== 缓存过期时间 ====================
    
    /**
     * 用户交互聚合根缓存过期时间（30分钟）
     */
    public static final Duration USER_INTERACTION_TTL = Duration.ofMinutes(30);
    
    /**
     * 收藏状态缓存过期时间（1小时）
     */
    public static final Duration FAVORITE_STATUS_TTL = Duration.ofHours(1);
    
    /**
     * 收藏列表缓存过期时间（10分钟）
     */
    public static final Duration FAVORITES_LIST_TTL = Duration.ofMinutes(10);
    
    /**
     * 统计数据缓存过期时间（15分钟）
     */
    public static final Duration COUNT_DATA_TTL = Duration.ofMinutes(15);
    
    /**
     * 评论列表缓存过期时间（10分钟）
     */
    public static final Duration COMMENTS_LIST_TTL = Duration.ofMinutes(10);
    
    /**
     * 空结果缓存过期时间（2分钟）- 防止缓存穿透
     */
    public static final Duration NULL_RESULT_TTL = Duration.ofMinutes(2);
    
    // ==================== 缓存配置参数 ====================
    
    /**
     * 缓存键前缀
     */
    public static final String CACHE_PREFIX = "relics:interaction:";
    
    /**
     * 分布式锁超时时间（秒）
     */
    public static final long LOCK_TIMEOUT_SECONDS = 10L;
    
    /**
     * 分布式锁等待时间（秒）
     */
    public static final long LOCK_WAIT_SECONDS = 3L;
    
    /**
     * 批量操作最大大小
     */
    public static final int BATCH_SIZE_LIMIT = 1000;
    
    /**
     * 缓存预热延迟时间（毫秒）
     */
    public static final long CACHE_WARM_UP_DELAY = 100L;
    
    /**
     * 随机过期时间范围（分钟）- 防止缓存雪崩
     */
    public static final int RANDOM_TTL_RANGE_MINUTES = 5;
    
    // ==================== 特殊值定义 ====================
    
    /**
     * 空结果标记值
     */
    public static final String NULL_VALUE = "NULL";
    
    /**
     * 全部文物ID标记（用于查询用户所有评论）
     */
    public static final String ALL_RELICS_MARKER = "ALL";
    
    // ==================== 工具方法 ====================
    
    /**
     * 生成带随机过期时间的TTL，防止缓存雪崩
     * @param baseTtl 基础过期时间
     * @return 带随机偏移的过期时间
     */
    public static Duration getRandomizedTtl(Duration baseTtl) {
        long randomOffset = (long) (Math.random() * RANDOM_TTL_RANGE_MINUTES * 60);
        return baseTtl.plusSeconds(randomOffset);
    }
    
    /**
     * 获取分布式锁键
     * @param businessKey 业务键
     * @return 锁键
     */
    public static String getLockKey(String businessKey) {
        return CACHE_PREFIX + "lock:" + businessKey;
    }
    
    /**
     * 检查是否为空值标记
     * @param value 缓存值
     * @return 是否为空值标记
     */
    public static boolean isNullValue(Object value) {
        return NULL_VALUE.equals(value);
    }
    
    /**
     * 私有构造函数，防止实例化
     */
    private UserInteractionCacheConfig() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
