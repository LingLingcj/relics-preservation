package com.ling.domain.interaction.model.valobj;

import lombok.Getter;

/**
 * 审核操作枚举
 * @Author: LingRJ
 * @Description: 定义评论审核的操作类型
 * @DateTime: 2025/7/11
 */
@Getter
public enum ReviewAction {
    
    APPROVE("通过", "审核通过，评论可以公开显示"),
    REJECT("拒绝", "审核拒绝，评论不会公开显示");
    
    private final String name;
    private final String description;
    
    ReviewAction(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * 根据名称获取操作
     */
    public static ReviewAction fromName(String name) {
        for (ReviewAction action : values()) {
            if (action.name.equals(name)) {
                return action;
            }
        }
        throw new IllegalArgumentException("无效的审核操作: " + name);
    }
    
    /**
     * 转换为对应的评论状态
     */
    public CommentStatus toCommentStatus() {
        return switch (this) {
            case APPROVE -> CommentStatus.APPROVED;
            case REJECT -> CommentStatus.REJECTED;
        };
    }
    
    /**
     * 是否为通过操作
     */
    public boolean isApprove() {
        return this == APPROVE;
    }
    
    /**
     * 是否为拒绝操作
     */
    public boolean isReject() {
        return this == REJECT;
    }
}
