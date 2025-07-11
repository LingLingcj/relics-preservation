package com.ling.domain.interaction.event;

import com.ling.types.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户评论文物事件
 * @Author: LingRJ
 * @Description: 当用户评论文物时发布的领域事件
 * @DateTime: 2025/7/11
 */
@Getter
public class UserCommentedOnRelicsEvent implements DomainEvent {
    
    private final String username;
    private final Long relicsId;
    private final String content;
    private final Long commentId;
    private final LocalDateTime occurredOn;
    private final String eventId;
    
    public UserCommentedOnRelicsEvent(String username, Long relicsId, String content, Long commentId) {
        this.username = username;
        this.relicsId = relicsId;
        this.content = content;
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
        return "UserCommentedOnRelics";
    }
    
    @Override
    public String eventId() {
        return eventId;
    }
    
    /**
     * 获取内容摘要
     * @return 内容摘要
     */
    public String getContentSummary() {
        if (content == null || content.length() <= 50) {
            return content;
        }
        return content.substring(0, 50) + "...";
    }
    
    @Override
    public String toString() {
        return String.format("UserCommentedOnRelicsEvent{username='%s', relicsId=%d, commentId=%d, content='%s', occurredOn=%s}", 
                username, relicsId, commentId, getContentSummary(), occurredOn);
    }
}
