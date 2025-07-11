package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

/**
 * 评论状态枚举
 * @Author: LingRJ
 * @Description: 定义评论的审核状态
 * @DateTime: 2025/7/11
 */
@Getter
public enum CommentStatus {
    
    PENDING_REVIEW(0, "待审核", "评论已提交，等待审核"),
    APPROVED(1, "已通过", "评论已通过审核，可以公开显示"),
    REJECTED(2, "已拒绝", "评论未通过审核，不会公开显示");
    
    private final int code;
    private final String name;
    private final String description;
    
    CommentStatus(int code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    /**
     * 根据代码获取状态
     */
    public static CommentStatus fromCode(int code) {
        for (CommentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的评论状态代码: " + code);
    }
    
    /**
     * 是否为待审核状态
     */
    public boolean isPending() {
        return this == PENDING_REVIEW;
    }
    
    /**
     * 是否已通过审核
     */
    public boolean isApproved() {
        return this == APPROVED;
    }
    
    /**
     * 是否被拒绝
     */
    public boolean isRejected() {
        return this == REJECTED;
    }
    
    /**
     * 是否可以公开显示
     */
    public boolean isPublic() {
        return this == APPROVED;
    }
    
    /**
     * 是否可以被审核
     */
    public boolean canBeReviewed() {
        return this == PENDING_REVIEW;
    }
}
