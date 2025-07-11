package com.ling.domain.interaction.model.valobj;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 包含用户信息的评论值对象
 * @Author: LingRJ
 * @Description: 封装评论信息和对应的用户信息
 * @DateTime: 2025/7/11
 */
@Getter
@Builder
public class CommentWithUser {
    
    /** 评论ID */
    private final Long commentId;
    
    /** 文物ID */
    private final Long relicsId;
    
    /** 用户名 */
    private final String username;
    
    /** 评论内容 */
    private final String content;
    
    /** 评论状态 */
    private final CommentStatus status;
    
    /** 创建时间 */
    private final LocalDateTime createTime;
    
    /** 更新时间 */
    private final LocalDateTime updateTime;
    
    /** 是否已删除 */
    private final boolean deleted;
    
    /**
     * 从 CommentAction 和用户名创建
     */
    public static CommentWithUser from(CommentAction comment, String username) {
        return CommentWithUser.builder()
                .commentId(comment.getId())
                .relicsId(comment.getRelicsId())
                .username(username)
                .content(comment.getFullContent())
                .status(comment.getStatus())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .deleted(comment.isDeleted())
                .build();
    }
    
    /**
     * 是否可见
     */
    public boolean isVisible() {
        return !deleted && status == CommentStatus.APPROVED;
    }
    
    /**
     * 是否需要审核
     */
    public boolean needsReview() {
        return status == CommentStatus.PENDING_REVIEW;
    }
    
    /**
     * 获取等待审核天数
     */
    public long getWaitingDays() {
        if (status != CommentStatus.PENDING_REVIEW) {
            return 0;
        }
        return java.time.Duration.between(createTime, LocalDateTime.now()).toDays();
    }
    
    /**
     * 是否紧急（等待审核超过3天）
     */
    public boolean isUrgent() {
        return getWaitingDays() > 3;
    }
    
    /**
     * 获取内容摘要
     */
    public String getContentSummary() {
        if (content == null || content.length() <= 50) {
            return content;
        }
        return content.substring(0, 50) + "...";
    }
    
    @Override
    public String toString() {
        return String.format("CommentWithUser{id=%d, username='%s', relicsId=%d, status=%s}", 
                commentId, username, relicsId, status);
    }
}
