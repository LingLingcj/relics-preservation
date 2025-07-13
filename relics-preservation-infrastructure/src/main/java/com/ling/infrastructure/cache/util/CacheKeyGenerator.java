package com.ling.infrastructure.cache.util;

import com.ling.domain.user.model.valobj.Username;
import com.ling.infrastructure.cache.config.UserInteractionCacheConfig;

/**
 * 缓存键生成器
 * @Author: LingRJ
 * @Description: 负责生成各种缓存键，确保键的一致性和规范性
 * @DateTime: 2025/7/13
 */
public final class CacheKeyGenerator {
    
    /**
     * 生成用户交互聚合根缓存键
     * @param username 用户名
     * @return 缓存键
     */
    public static String userInteractionKey(Username username) {
        return String.format(UserInteractionCacheConfig.USER_INTERACTION_KEY, 
                           username.getValue());
    }
    
    /**
     * 生成用户收藏状态缓存键
     * @param username 用户名
     * @param relicsId 文物ID
     * @return 缓存键
     */
    public static String userFavoriteStatusKey(Username username, Long relicsId) {
        return String.format(UserInteractionCacheConfig.USER_FAVORITE_STATUS_KEY, 
                           username.getValue(), relicsId);
    }
    
    /**
     * 生成用户收藏列表缓存键
     * @param username 用户名
     * @return 缓存键
     */
    public static String userFavoritesListKey(Username username) {
        return String.format(UserInteractionCacheConfig.USER_FAVORITES_LIST_KEY, 
                           username.getValue());
    }
    
    /**
     * 生成文物收藏数量缓存键
     * @param relicsId 文物ID
     * @return 缓存键
     */
    public static String relicsFavoriteCountKey(Long relicsId) {
        return String.format(UserInteractionCacheConfig.RELICS_FAVORITE_COUNT_KEY, 
                           relicsId);
    }
    
    /**
     * 生成用户评论列表缓存键
     * @param username 用户名
     * @param relicsId 文物ID（可为null，表示所有评论）
     * @return 缓存键
     */
    public static String userCommentsKey(Username username, Long relicsId) {
        String relicsIdStr = relicsId != null ? relicsId.toString() : 
                           UserInteractionCacheConfig.ALL_RELICS_MARKER;
        return String.format(UserInteractionCacheConfig.USER_COMMENTS_KEY, 
                           username.getValue(), relicsIdStr);
    }
    
    /**
     * 生成文物评论数量缓存键
     * @param relicsId 文物ID
     * @return 缓存键
     */
    public static String relicsCommentCountKey(Long relicsId) {
        return String.format(UserInteractionCacheConfig.RELICS_COMMENT_COUNT_KEY, 
                           relicsId);
    }
    
    /**
     * 生成文物已审核评论列表缓存键
     * @param relicsId 文物ID
     * @return 缓存键
     */
    public static String relicsApprovedCommentsKey(Long relicsId) {
        return String.format(UserInteractionCacheConfig.RELICS_APPROVED_COMMENTS_KEY, 
                           relicsId);
    }
    
    /**
     * 生成用户收藏总数缓存键
     * @param username 用户名
     * @return 缓存键
     */
    public static String userFavoriteCountKey(Username username) {
        return String.format(UserInteractionCacheConfig.USER_FAVORITE_COUNT_KEY, 
                           username.getValue());
    }
    
    /**
     * 生成用户评论总数缓存键
     * @param username 用户名
     * @param relicsId 文物ID（可为null，表示所有评论）
     * @return 缓存键
     */
    public static String userCommentCountKey(Username username, Long relicsId) {
        String relicsIdStr = relicsId != null ? relicsId.toString() : 
                           UserInteractionCacheConfig.ALL_RELICS_MARKER;
        return String.format(UserInteractionCacheConfig.USER_COMMENT_COUNT_KEY, 
                           username.getValue(), relicsIdStr);
    }
    
    /**
     * 生成分页缓存键后缀
     * @param page 页码
     * @param size 页大小
     * @return 分页后缀
     */
    public static String pageKeySuffix(int page, int size) {
        return String.format(":page:%d:size:%d", page, size);
    }
    
    /**
     * 生成带分页的用户收藏列表缓存键
     * @param username 用户名
     * @param page 页码
     * @param size 页大小
     * @return 缓存键
     */
    public static String userFavoritesListKey(Username username, int page, int size) {
        return userFavoritesListKey(username) + pageKeySuffix(page, size);
    }
    
    /**
     * 生成带分页的用户评论列表缓存键
     * @param username 用户名
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 页大小
     * @return 缓存键
     */
    public static String userCommentsKey(Username username, Long relicsId, int page, int size) {
        return userCommentsKey(username, relicsId) + pageKeySuffix(page, size);
    }
    
    /**
     * 生成带分页的文物已审核评论列表缓存键
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 页大小
     * @return 缓存键
     */
    public static String relicsApprovedCommentsKey(Long relicsId, int page, int size) {
        return relicsApprovedCommentsKey(relicsId) + pageKeySuffix(page, size);
    }
    
    /**
     * 生成用户相关的所有缓存键模式（用于批量删除）
     * @param username 用户名
     * @return 缓存键模式
     */
    public static String userRelatedKeysPattern(Username username) {
        return String.format("*%s*", username.getValue());
    }
    
    /**
     * 生成文物相关的所有缓存键模式（用于批量删除）
     * @param relicsId 文物ID
     * @return 缓存键模式
     */
    public static String relicsRelatedKeysPattern(Long relicsId) {
        return String.format("*%d*", relicsId);
    }
    
    /**
     * 生成分布式锁键
     * @param businessKey 业务键
     * @return 锁键
     */
    public static String lockKey(String businessKey) {
        return UserInteractionCacheConfig.getLockKey(businessKey);
    }
    
    /**
     * 私有构造函数，防止实例化
     */
    private CacheKeyGenerator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
