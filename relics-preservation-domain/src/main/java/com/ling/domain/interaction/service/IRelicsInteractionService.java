package com.ling.domain.interaction.service;

import com.ling.domain.interaction.model.valobj.*;

import java.util.List;

/**
 * 文物交互服务接口
 * @Author: LingRJ
 * @Description: 处理文物相关的交互业务逻辑
 * @DateTime: 2025/7/11
 */
public interface IRelicsInteractionService {
    
    // ==================== 收藏统计 ====================
    
    /**
     * 获取文物收藏数量
     * @param relicsId 文物ID
     * @return 收藏数量
     */
    long getRelicsFavoriteCount(Long relicsId);
    
    /**
     * 获取文物收藏用户列表
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 收藏用户列表
     */
    FavoriteUserListResult getRelicsFavoriteUsers(Long relicsId, int page, int size);
    
    /**
     * 检查文物是否被用户收藏
     * @param relicsId 文物ID
     * @param username 用户名
     * @return 是否被收藏
     */
    boolean isRelicsFavoritedByUser(Long relicsId, String username);
    
    // ==================== 评论管理 ====================
    
    /**
     * 获取文物评论数量
     * @param relicsId 文物ID
     * @return 评论数量
     */
    long getRelicsCommentCount(Long relicsId);
    
    /**
     * 获取文物评论列表
     * @param relicsId 文物ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    RelicsCommentListResult getRelicsComments(Long relicsId, int page, int size);
    
    /**
     * 获取文物最新评论
     * @param relicsId 文物ID
     * @param limit 限制数量
     * @return 最新评论列表
     */
    List<CommentAction> getRelicsLatestComments(Long relicsId, int limit);
    
    /**
     * 获取待审核的评论
     * @param relicsId 文物ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 待审核评论列表
     */
    PendingCommentListResult getPendingComments(Long relicsId, int page, int size);
    
    // ==================== 交互统计 ====================
    
    /**
     * 获取文物交互统计
     * @param relicsId 文物ID
     * @return 交互统计信息
     */
    RelicsInteractionStatistics getRelicsStatistics(Long relicsId);
    
    /**
     * 获取热门文物列表（按交互数量排序）
     * @param limit 限制数量
     * @return 热门文物列表
     */
    List<RelicsInteractionSummary> getPopularRelics(int limit);
    
    /**
     * 获取最近有交互的文物
     * @param limit 限制数量
     * @return 最近交互文物列表
     */
    List<RelicsInteractionSummary> getRecentlyInteractedRelics(int limit);
    
    // ==================== 批量操作 ====================
    
    /**
     * 批量获取文物收藏数量
     * @param relicsIds 文物ID列表
     * @return 收藏数量映射
     */
    java.util.Map<Long, Long> batchGetRelicsFavoriteCounts(List<Long> relicsIds);
    
    /**
     * 批量获取文物评论数量
     * @param relicsIds 文物ID列表
     * @return 评论数量映射
     */
    java.util.Map<Long, Long> batchGetRelicsCommentCounts(List<Long> relicsIds);
    
    // ==================== 结果对象 ====================
    
    /**
     * 收藏用户列表结果
     */
    record FavoriteUserListResult(
            List<String> usernames,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
    
    /**
     * 文物评论列表结果
     */
    record RelicsCommentListResult(
            List<CommentAction> comments,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
    
    /**
     * 待审核评论列表结果
     */
    record PendingCommentListResult(
            List<CommentAction> comments,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
    
    /**
     * 文物交互统计
     */
    record RelicsInteractionStatistics(
            Long relicsId,
            long favoriteCount,
            long commentCount,
            long totalInteractions,
            java.time.LocalDateTime lastInteractionTime,
            double popularityScore
    ) {}
    
    /**
     * 文物交互摘要
     */
    record RelicsInteractionSummary(
            Long relicsId,
            String relicsName,
            long favoriteCount,
            long commentCount,
            java.time.LocalDateTime lastInteractionTime
    ) {}
}
