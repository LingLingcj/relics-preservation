package com.ling.domain.interaction.model.valobj;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 交互活动值对象
 * @Author: LingRJ
 * @Description: 封装用户的交互活动记录
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
public class InteractionActivity {
    
    private final String username;
    private final Long relicsId;
    private final String relicsName;
    private final ActivityType activityType;
    private final String activityDescription;
    private final LocalDateTime activityTime;
    private final Object activityData;
    
    /**
     * 创建收藏活动
     * @param username 用户名
     * @param relicsId 文物ID
     * @param relicsName 文物名称
     * @param isFavorite 是否收藏
     * @return 交互活动
     */
    public static InteractionActivity createFavoriteActivity(String username, Long relicsId, 
                                                           String relicsName, boolean isFavorite) {
        return InteractionActivity.builder()
                .username(username)
                .relicsId(relicsId)
                .relicsName(relicsName)
                .activityType(isFavorite ? ActivityType.FAVORITE_ADDED : ActivityType.FAVORITE_REMOVED)
                .activityDescription(isFavorite ? "收藏了文物" : "取消收藏文物")
                .activityTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建评论活动
     * @param username 用户名
     * @param relicsId 文物ID
     * @param relicsName 文物名称
     * @param commentId 评论ID
     * @param contentSummary 评论摘要
     * @return 交互活动
     */
    public static InteractionActivity createCommentActivity(String username, Long relicsId, 
                                                          String relicsName, Long commentId, String contentSummary) {
        return InteractionActivity.builder()
                .username(username)
                .relicsId(relicsId)
                .relicsName(relicsName)
                .activityType(ActivityType.COMMENT_ADDED)
                .activityDescription("评论了文物: " + contentSummary)
                .activityTime(LocalDateTime.now())
                .activityData(commentId)
                .build();
    }
    
    /**
     * 创建评论删除活动
     * @param username 用户名
     * @param relicsId 文物ID
     * @param relicsName 文物名称
     * @param commentId 评论ID
     * @return 交互活动
     */
    public static InteractionActivity createCommentDeleteActivity(String username, Long relicsId, 
                                                                String relicsName, Long commentId) {
        return InteractionActivity.builder()
                .username(username)
                .relicsId(relicsId)
                .relicsName(relicsName)
                .activityType(ActivityType.COMMENT_DELETED)
                .activityDescription("删除了评论")
                .activityTime(LocalDateTime.now())
                .activityData(commentId)
                .build();
    }
    
    /**
     * 是否为最近活动（24小时内）
     * @return 是否为最近活动
     */
    public boolean isRecentActivity() {
        return activityTime.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    /**
     * 获取活动时长（小时）
     * @return 活动时长
     */
    public long getActivityHours() {
        return java.time.Duration.between(activityTime, LocalDateTime.now()).toHours();
    }
    
    /**
     * 获取活动数据（泛型）
     * @param clazz 数据类型
     * @param <T> 泛型类型
     * @return 活动数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getActivityData(Class<T> clazz) {
        if (activityData != null && clazz.isInstance(activityData)) {
            return (T) activityData;
        }
        return null;
    }
    
    /**
     * 活动类型枚举
     */
    public enum ActivityType {
        FAVORITE_ADDED("添加收藏"),
        FAVORITE_REMOVED("取消收藏"),
        COMMENT_ADDED("添加评论"),
        COMMENT_DELETED("删除评论"),
        COMMENT_APPROVED("评论通过审核"),
        COMMENT_REJECTED("评论被拒绝");
        
        private final String description;
        
        ActivityType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Override
    public String toString() {
        return String.format("InteractionActivity{user='%s', relics='%s', type=%s, time=%s}", 
                username, relicsName, activityType.getDescription(), activityTime);
    }
}
