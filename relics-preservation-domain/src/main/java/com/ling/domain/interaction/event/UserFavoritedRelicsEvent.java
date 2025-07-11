package com.ling.domain.interaction.event;

import com.ling.types.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户收藏文物事件
 * @Author: LingRJ
 * @Description: 当用户收藏文物时发布的领域事件
 * @DateTime: 2025/7/11
 */
@Getter
public class UserFavoritedRelicsEvent implements DomainEvent {
    
    private final String username;
    private final Long relicsId;
    private final LocalDateTime occurredOn;
    private final String eventId;
    
    public UserFavoritedRelicsEvent(String username, Long relicsId) {
        this.username = username;
        this.relicsId = relicsId;
        this.occurredOn = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String eventType() {
        return "UserFavoritedRelics";
    }
    
    @Override
    public String eventId() {
        return eventId;
    }
    
    @Override
    public String toString() {
        return String.format("UserFavoritedRelicsEvent{username='%s', relicsId=%d, occurredOn=%s}", 
                username, relicsId, occurredOn);
    }
}
