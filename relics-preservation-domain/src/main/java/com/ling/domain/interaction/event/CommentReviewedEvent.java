package com.ling.domain.interaction.event;

import com.ling.domain.interaction.model.valobj.CommentReviewResult;
import com.ling.domain.interaction.model.valobj.ReviewAction;
import com.ling.types.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 评论审核事件
 * @Author: LingRJ
 * @Description: 当评论被审核时触发的领域事件
 * @DateTime: 2025/7/11
 */
@Getter
public class CommentReviewedEvent implements DomainEvent {
    
    private final String eventId;
    private final Long commentId;
    private final String commentAuthor;
    private final Long relicsId;
    private final ReviewAction action;
    private final String reviewer;
    private final String reason;
    private final LocalDateTime reviewTime;
    private final boolean approved;
    private final LocalDateTime occurredOn;
    
    public CommentReviewedEvent(CommentReviewResult reviewResult, String commentAuthor, Long relicsId) {
        this.eventId = UUID.randomUUID().toString();
        this.commentId = reviewResult.getCommentId();
        this.commentAuthor = commentAuthor;
        this.relicsId = relicsId;
        this.action = reviewResult.getAction();
        this.reviewer = reviewResult.getReviewer();
        this.reason = reviewResult.getReason();
        this.reviewTime = reviewResult.getReviewTime();
        this.approved = reviewResult.isApproved();
        this.occurredOn = LocalDateTime.now();
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventType() {
        return "CommentReviewed";
    }

    /**
     * 获取事件描述
     */
    public String getEventDescription() {
        return String.format("评论 %d (作者: %s, 文物: %d) 被 %s %s", 
                commentId, commentAuthor, relicsId, reviewer, action.getName());
    }
    
    /**
     * 是否需要通知用户
     */
    public boolean shouldNotifyUser() {
        return true; // 审核结果都需要通知用户
    }
    
    /**
     * 获取通知消息
     */
    public String getNotificationMessage() {
        if (approved) {
            return String.format("您对文物 %d 的评论已通过审核", relicsId);
        } else {
            return String.format("您对文物 %d 的评论未通过审核，原因：%s", 
                    relicsId, reason != null ? reason : "不符合社区规范");
        }
    }
}
