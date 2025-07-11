package com.ling.domain.interaction.model.valobj;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 评论审核结果值对象
 * @Author: LingRJ
 * @Description: 表示评论审核的结果信息
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
public class CommentReviewResult {
    
    /** 评论ID */
    private final Long commentId;
    
    /** 审核操作 */
    private final ReviewAction action;
    
    /** 审核人 */
    private final String reviewer;
    
    /** 审核理由 */
    private final String reason;
    
    /** 审核时间 */
    private final LocalDateTime reviewTime;
    
    /** 审核前状态 */
    private final CommentStatus beforeStatus;
    
    /** 审核后状态 */
    private final CommentStatus afterStatus;
    
    /** 是否成功 */
    private final boolean success;
    
    /** 错误信息 */
    private final String errorMessage;
    
    /**
     * 创建成功的审核结果
     */
    public static CommentReviewResult success(Long commentId, ReviewAction action, 
                                            String reviewer, String reason,
                                            CommentStatus beforeStatus, CommentStatus afterStatus) {
        return CommentReviewResult.builder()
                .commentId(commentId)
                .action(action)
                .reviewer(reviewer)
                .reason(reason)
                .reviewTime(LocalDateTime.now())
                .beforeStatus(beforeStatus)
                .afterStatus(afterStatus)
                .success(true)
                .build();
    }
    
    /**
     * 创建失败的审核结果
     */
    public static CommentReviewResult failure(Long commentId, ReviewAction action, 
                                            String reviewer, String errorMessage) {
        return CommentReviewResult.builder()
                .commentId(commentId)
                .action(action)
                .reviewer(reviewer)
                .reviewTime(LocalDateTime.now())
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 获取审核结果描述
     */
    public String getResultDescription() {
        if (!success) {
            return "审核失败: " + errorMessage;
        }
        
        return String.format("评论 %d 已被 %s %s，理由：%s", 
                commentId, reviewer, action.getName(), 
                reason != null ? reason : "无");
    }
    
    /**
     * 是否为通过审核
     */
    public boolean isApproved() {
        return success && action.isApprove();
    }
    
    /**
     * 是否为拒绝审核
     */
    public boolean isRejected() {
        return success && action.isReject();
    }
}
