package com.ling.domain.user.event;

import com.ling.types.event.DomainEvent;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 密码修改事件
 * @Author: LingRJ
 * @Description: 当用户密码被修改时发布此事件
 * @DateTime: 2025/7/11
 */
@Getter
public class PasswordChangedEvent implements DomainEvent {
    
    private final String eventId;
    private final String username;
    private final String changeReason;
    private final LocalDateTime occurredOn;
    
    public PasswordChangedEvent(String username, String changeReason) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.username = username;
        this.changeReason = changeReason;
        this.occurredOn = LocalDateTime.now();
    }
    
    public PasswordChangedEvent(String username) {
        this(username, "用户主动修改");
    }

    @Override
    public LocalDateTime occurredOn() {
        return this.occurredOn;
    }

    @Override
    public String eventType() {
        return "PasswordChanged";
    }
    
    @Override
    public String toString() {
        return String.format("PasswordChangedEvent{eventId='%s', username='%s', changeReason='%s', occurredOn=%s}", 
                eventId, username, changeReason, occurredOn);
    }
}
