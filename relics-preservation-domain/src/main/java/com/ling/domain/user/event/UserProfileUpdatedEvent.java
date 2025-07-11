package com.ling.domain.user.event;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户资料更新事件
 * @Author: LingRJ
 * @Description: 当用户资料被更新时发布此事件
 * @DateTime: 2025/7/11
 */
@Getter
public class UserProfileUpdatedEvent implements DomainEvent {
    
    private final String eventId;
    private final String username;
    private final Map<String, Object> updatedFields;
    private final LocalDateTime occurredOn;
    
    public UserProfileUpdatedEvent(String username, Map<String, Object> updatedFields) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.username = username;
        this.updatedFields = updatedFields;
        this.occurredOn = LocalDateTime.now();
    }

    @Override
    public LocalDateTime occurredOn() {
        return LocalDateTime.now();
    }

    @Override
    public String eventType() {
        return "UserProfileUpdated";
    }
    
    /**
     * 检查特定字段是否被更新
     * @param fieldName 字段名
     * @return 是否被更新
     */
    public boolean isFieldUpdated(String fieldName) {
        return updatedFields.containsKey(fieldName);
    }
    
    /**
     * 获取更新字段的新值
     * @param fieldName 字段名
     * @return 新值
     */
    public Object getUpdatedValue(String fieldName) {
        return updatedFields.get(fieldName);
    }
    
    @Override
    public String toString() {
        return String.format("UserProfileUpdatedEvent{eventId='%s', username='%s', updatedFields=%s, occurredOn=%s}", 
                eventId, username, updatedFields.keySet(), occurredOn);
    }
}
