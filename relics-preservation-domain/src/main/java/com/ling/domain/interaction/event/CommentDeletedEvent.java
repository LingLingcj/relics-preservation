package com.ling.domain.interaction.event;

import com.ling.types.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 评论删除事件
 * @Author: LingRJ
 * @Description: 当评论被删除时发布的领域事件
 * @DateTime: 2025/7/11
 */
@Getter
public class CommentDeletedEvent implements DomainEvent {
    
    private final String username;
    private final Long relicsId;
    private final Long commentId;
    private final LocalDateTime occurredOn;
    private final String eventId;
    
    public CommentDeletedEvent(String username, Long relicsId, Long commentId) {
        this.username = username;
        this.relicsId = relicsId;
        this.commentId = commentId;
        this.occurredOn = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "CommentDeleted";
    }
    
    @Override
    public String eventId() {
        return eventId;
    }
    
    @Override
    public String toString() {
        return String.format("CommentDeletedEvent{username='%s', relicsId=%d, commentId=%d, occurredOn=%s}", 
                username, relicsId, commentId, occurredOn);
    }
}
