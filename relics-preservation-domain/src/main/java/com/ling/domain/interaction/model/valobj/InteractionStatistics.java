package com.ling.domain.interaction.model.valobj;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 交互统计值对象
 * @Author: LingRJ
 * @Description: 封装用户交互统计信息
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
public class InteractionStatistics {
    
    private final String username;
    private final long favoriteCount;
    private final long commentCount;
    private final long totalInteractions;
    private final LocalDateTime lastActiveTime;
    private final LocalDateTime firstInteractionTime;
    
    /**
     * 获取总交互次数
     * @return 总交互次数
     */
    public long getTotalInteractions() {
        return favoriteCount + commentCount;
    }
    
    /**
     * 是否为活跃用户（最近7天有交互）
     * @return 是否为活跃用户
     */
    public boolean isActiveUser() {
        if (lastActiveTime == null) {
            return false;
        }
        return lastActiveTime.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * 是否为新用户（首次交互在30天内）
     * @return 是否为新用户
     */
    public boolean isNewUser() {
        if (firstInteractionTime == null) {
            return true;
        }
        return firstInteractionTime.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * 获取用户活跃度等级
     * @return 活跃度等级
     */
    public ActivityLevel getActivityLevel() {
        long total = getTotalInteractions();
        if (total == 0) {
            return ActivityLevel.INACTIVE;
        } else if (total < 10) {
            return ActivityLevel.LOW;
        } else if (total < 50) {
            return ActivityLevel.MEDIUM;
        } else if (total < 200) {
            return ActivityLevel.HIGH;
        } else {
            return ActivityLevel.VERY_HIGH;
        }
    }
    
    /**
     * 获取收藏评论比例
     * @return 收藏评论比例
     */
    public double getFavoriteCommentRatio() {
        if (commentCount == 0) {
            return favoriteCount > 0 ? Double.MAX_VALUE : 0.0;
        }
        return (double) favoriteCount / commentCount;
    }
    
    /**
     * 活跃度等级枚举
     */
    public enum ActivityLevel {
        INACTIVE("不活跃", 0),
        LOW("低活跃", 1),
        MEDIUM("中等活跃", 2),
        HIGH("高活跃", 3),
        VERY_HIGH("非常活跃", 4);
        
        private final String description;
        private final int level;
        
        ActivityLevel(String description, int level) {
            this.description = description;
            this.level = level;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    @Override
    public String toString() {
        return String.format("InteractionStatistics{username='%s', favorites=%d, comments=%d, level=%s}", 
                username, favoriteCount, commentCount, getActivityLevel().getDescription());
    }
}
