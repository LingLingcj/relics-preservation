package com.ling.domain.user.event;

import com.ling.types.event.DomainEvent;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 用户登录事件
 * @Author: LingRJ
 * @Description: 当用户成功登录时发布此事件
 * @DateTime: 2025/7/11
 */
@Getter
public class UserLoggedInEvent implements DomainEvent {
    
    private final String eventId;
    private final String username;
    private final String role;
    private final String loginIp;
    private final LocalDateTime occurredOn;
    
    public UserLoggedInEvent(String username, String role, String loginIp) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.username = username;
        this.role = role;
        this.loginIp = loginIp;
        this.occurredOn = LocalDateTime.now();
    }
    
    public UserLoggedInEvent(String username, String role) {
        this(username, role, null);
    }

    @Override
    public LocalDateTime occurredOn() {
        return this.occurredOn;
    }

    @Override
    public String eventType() {
        return "UserLoggedIn";
    }
    
    @Override
    public String toString() {
        return String.format("UserLoggedInEvent{eventId='%s', username='%s', role='%s', loginIp='%s', occurredOn=%s}", 
                eventId, username, role, loginIp, occurredOn);
    }
}
