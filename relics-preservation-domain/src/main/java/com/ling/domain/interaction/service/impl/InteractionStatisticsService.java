package com.ling.domain.interaction.service.impl;

import com.ling.domain.interaction.service.IInteractionStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 交互统计领域服务实现 - 纯业务逻辑
 * @Author: LingRJ
 * @Description: 专门处理统计计算的业务逻辑，不依赖任何基础设施
 * @DateTime: 2025/7/11
 */
@Slf4j
@Service
public class InteractionStatisticsService {
    
    /**
     * 合并热门文物数据 - 业务规则：按总交互数排序
     */
    public List<IInteractionStatisticsService.RelicsInteractionSummary> mergePopularRelics(
            List<FavoriteData> favoriteData,
            List<CommentData> commentData,
            int limit) {
        
        Map<Long, MergeableRelicsData> dataMap = new HashMap<>();
        
        // 处理收藏数据
        favoriteData.forEach(fav -> 
            dataMap.put(fav.relicsId(), new MergeableRelicsData(
                fav.relicsId(), fav.relicsName(), fav.favoriteCount(), 0L, fav.lastFavoriteTime())));
        
        // 合并评论数据
        commentData.forEach(comment -> 
            dataMap.merge(comment.relicsId(), 
                new MergeableRelicsData(comment.relicsId(), comment.relicsName(), 0L, 
                    comment.commentCount(), comment.lastCommentTime()),
                MergeableRelicsData::merge));
        
        // 业务规则：按总交互数排序，相同时按最近交互时间排序
        return dataMap.values().stream()
                .sorted((d1, d2) -> {
                    long total1 = d1.favoriteCount + d1.commentCount;
                    long total2 = d2.favoriteCount + d2.commentCount;
                    if (total1 != total2) {
                        return Long.compare(total2, total1); // 降序
                    }
                    return d2.lastInteractionTime.compareTo(d1.lastInteractionTime);
                })
                .limit(limit)
                .map(MergeableRelicsData::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 合并最近交互文物数据 - 业务规则：按最近交互时间排序
     */
    public List<IInteractionStatisticsService.RelicsInteractionSummary> mergeRecentRelics(
            List<FavoriteData> favoriteData,
            List<CommentData> commentData,
            int limit) {
        
        Map<Long, MergeableRelicsData> dataMap = new HashMap<>();
        
        // 处理收藏数据
        favoriteData.forEach(fav -> 
            dataMap.put(fav.relicsId(), new MergeableRelicsData(
                fav.relicsId(), fav.relicsName(), fav.favoriteCount(), 0L, fav.lastFavoriteTime())));
        
        // 合并评论数据
        commentData.forEach(comment -> 
            dataMap.merge(comment.relicsId(), 
                new MergeableRelicsData(comment.relicsId(), comment.relicsName(), 0L, 
                    comment.commentCount(), comment.lastCommentTime()),
                MergeableRelicsData::merge));
        
        // 业务规则：按最近交互时间排序
        return dataMap.values().stream()
                .sorted((d1, d2) -> d2.lastInteractionTime.compareTo(d1.lastInteractionTime))
                .limit(limit)
                .map(MergeableRelicsData::toSummary)
                .collect(Collectors.toList());
    }

    /**
     * 合并活跃用户数据 - 业务规则：按活跃度评分排序
     */
    public List<String> mergeActiveUsers(
            List<UserFavoriteData> favoriteUsers,
            List<UserCommentData> commentUsers,
            int limit) {
        
        Map<String, MergeableUserData> dataMap = new HashMap<>();
        
        // 处理收藏用户数据
        favoriteUsers.forEach(user -> 
            dataMap.put(user.username(), new MergeableUserData(
                user.username(), user.favoriteCount(), 0L, user.lastFavoriteTime())));
        
        // 合并评论用户数据
        commentUsers.forEach(user -> 
            dataMap.merge(user.username(), 
                new MergeableUserData(user.username(), 0L, user.commentCount(), user.lastCommentTime()),
                MergeableUserData::merge));
        
        // 业务规则：按总活跃度排序，相同时按最近活跃时间排序
        return dataMap.values().stream()
                .sorted((u1, u2) -> {
                    long total1 = u1.favoriteCount + u1.commentCount;
                    long total2 = u2.favoriteCount + u2.commentCount;
                    if (total1 != total2) {
                        return Long.compare(total2, total1); // 降序
                    }
                    return u2.lastActiveTime.compareTo(u1.lastActiveTime);
                })
                .limit(limit)
                .map(data -> data.username)
                .collect(Collectors.toList());
    }
    
    /**
     * 计算热度分数 - 业务规则
     */
    public double calculatePopularityScore(long favoriteCount, long commentCount) {
        return favoriteCount * 2.0 + commentCount * 1.0;
    }

    // ==================== 数据传输对象 ====================
    
    /**
     * 收藏数据传输对象
     */
    public record FavoriteData(Long relicsId, String relicsName, long favoriteCount, LocalDateTime lastFavoriteTime) {}
    
    /**
     * 评论数据传输对象
     */
    public record CommentData(Long relicsId, String relicsName, long commentCount, LocalDateTime lastCommentTime) {}
    
    /**
     * 用户收藏数据传输对象
     */
    public record UserFavoriteData(String username, long favoriteCount, LocalDateTime lastFavoriteTime) {}
    
    /**
     * 用户评论数据传输对象
     */
    public record UserCommentData(String username, long commentCount, LocalDateTime lastCommentTime) {}

    // ==================== 内部业务对象 ====================
    
    /**
     * 可合并的文物数据内部类 - 领域概念
     */
    private static class MergeableRelicsData {
        final Long relicsId;
        final String relicsName;
        final long favoriteCount;
        final long commentCount;
        final LocalDateTime lastInteractionTime;
        
        MergeableRelicsData(Long relicsId, String relicsName, long favoriteCount, 
                           long commentCount, LocalDateTime lastInteractionTime) {
            this.relicsId = relicsId;
            this.relicsName = relicsName;
            this.favoriteCount = favoriteCount;
            this.commentCount = commentCount;
            this.lastInteractionTime = lastInteractionTime;
        }
        
        MergeableRelicsData merge(MergeableRelicsData other) {
            LocalDateTime latestTime = this.lastInteractionTime.isAfter(other.lastInteractionTime) 
                    ? this.lastInteractionTime : other.lastInteractionTime;
            return new MergeableRelicsData(
                    this.relicsId,
                    this.relicsName,
                    this.favoriteCount + other.favoriteCount,
                    this.commentCount + other.commentCount,
                    latestTime
            );
        }
        
        IInteractionStatisticsService.RelicsInteractionSummary toSummary() {
            return new IInteractionStatisticsService.RelicsInteractionSummary(relicsId, relicsName, favoriteCount, 
                    commentCount, lastInteractionTime);
        }
    }

    /**
     * 可合并的用户数据内部类 - 领域概念
     */
    private static class MergeableUserData {
        final String username;
        final long favoriteCount;
        final long commentCount;
        final LocalDateTime lastActiveTime;
        
        MergeableUserData(String username, long favoriteCount, long commentCount, 
                         LocalDateTime lastActiveTime) {
            this.username = username;
            this.favoriteCount = favoriteCount;
            this.commentCount = commentCount;
            this.lastActiveTime = lastActiveTime;
        }
        
        MergeableUserData merge(MergeableUserData other) {
            LocalDateTime latestTime = this.lastActiveTime.isAfter(other.lastActiveTime) 
                    ? this.lastActiveTime : other.lastActiveTime;
            return new MergeableUserData(
                    this.username,
                    this.favoriteCount + other.favoriteCount,
                    this.commentCount + other.commentCount,
                    latestTime
            );
        }
    }
}
