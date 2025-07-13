package com.ling.domain.interaction.service;

import com.ling.domain.interaction.model.valobj.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评论审核服务接口
 * @Author: LingRJ
 * @Description: 提供评论审核相关的业务逻辑
 * @DateTime: 2025/7/11
 */
public interface ICommentReviewService {
    
    // ==================== 单个审核 ====================
    
    /**
     * 审核单个评论
     * @param commentId 评论ID
     * @param action 审核操作
     * @param reviewer 审核人
     * @param reason 审核理由
     * @return 审核结果
     */
    CommentReviewResult reviewComment(Long commentId, ReviewAction action, String reviewer, String reason);

    /**
     * 删除不合适评论
     * @param commentId 评论id
     * @param reviewer 审核人
     * @param reason 删除原因
     * @return 删除结果
     */
    boolean deleteComment(Long commentId, String reviewer, String reason);
    
    // ==================== 批量审核 ====================
    
    /**
     * 批量审核评论
     * @param commentIds 评论ID列表
     * @param action 审核操作
     * @param reviewer 审核人
     * @param reason 审核理由
     * @return 批量审核结果
     */
    BatchReviewResult batchReviewComments(List<Long> commentIds, ReviewAction action, String reviewer, String reason);

    // ==================== 查询功能 ====================
    
    /**
     * 获取待审核评论列表
     * @param relicsId 文物ID（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 待审核评论列表
     */
    PendingCommentListResult getPendingComments(Long relicsId, int page, int size);
    
    /**
     * 获取审核历史
     * @param commentId 评论ID（可选）
     * @param reviewer 审核人（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 审核历史列表
     */
    ReviewHistoryListResult getReviewHistory(Long commentId, String reviewer, 
                                           LocalDateTime startTime, LocalDateTime endTime, 
                                           int page, int size);
    
    /**
     * 获取审核统计
     * @param reviewer 审核人（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 审核统计信息
     */
    ReviewStatistics getReviewStatistics(String reviewer, LocalDateTime startTime, LocalDateTime endTime);
    
    // ==================== 权限检查 ====================
    
    /**
     * 检查用户是否有审核权限
     * @param username 用户名
     * @return 是否有审核权限
     */
    boolean hasReviewPermission(String username);
    
    /**
     * 检查用户是否可以审核指定评论
     * @param username 用户名
     * @param commentId 评论ID
     * @return 是否可以审核
     */
    boolean canReviewComment(String username, Long commentId);
    
    // ==================== 结果对象 ====================
    
    /**
     * 批量审核结果
     */
    record BatchReviewResult(
            int totalCount,
            int successCount,
            int failureCount,
            List<CommentReviewResult> results,
            List<String> errors
    ) {
        public boolean isAllSuccess() {
            return failureCount == 0;
        }
        
        public double getSuccessRate() {
            return totalCount > 0 ? (double) successCount / totalCount : 0.0;
        }
    }
    
    /**
     * 待审核评论列表结果
     */
    record PendingCommentListResult(
            List<CommentWithUser> comments,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
    
    /**
     * 审核历史列表结果
     */
    record ReviewHistoryListResult(
            List<CommentReviewRecord> records,
            long total,
            int page,
            int size,
            boolean hasNext
    ) {}
    
    /**
     * 审核记录
     */
    record CommentReviewRecord(
            Long commentId,
            String commentContent,
            String commentAuthor,
            Long relicsId,
            ReviewAction action,
            String reviewer,
            String reason,
            LocalDateTime reviewTime,
            CommentStatus beforeStatus,
            CommentStatus afterStatus
    ) {}
    
    /**
     * 审核统计
     */
    record ReviewStatistics(
            String reviewer,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long totalReviewed,
            long approvedCount,
            long rejectedCount,
            double approvalRate,
            double rejectionRate,
            Map<String, Long> dailyStats
    ) {}
}
