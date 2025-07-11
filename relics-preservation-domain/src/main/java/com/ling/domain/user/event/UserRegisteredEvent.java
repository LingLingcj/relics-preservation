package com.ling.domain.user.event;

import com.ling.types.event.DomainEvent;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 用户注册事件
 * @Author: LingRJ
 * @Description: 当用户成功注册时发布此事件
 * @DateTime: 2025/7/11
 */
@Getter
public class UserRegisteredEvent implements DomainEvent {
    
    private final String eventId;
    private final String username;
    private final String role;
    private final String email;
    private final LocalDateTime occurredOn;
    
    public UserRegisteredEvent(String username, String role, String email) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.username = username;
        this.role = role;
        this.email = email;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() {
        return this.occurredOn;
    }

    @Override
    public String eventType() {
        return "UserRegistered";
    }
    
    @Override
    public String toString() {
        return String.format("UserRegisteredEvent{eventId='%s', username='%s', role='%s', occurredOn=%s}", 
                eventId, username, role, occurredOn);
    }
}
