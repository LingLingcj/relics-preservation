package com.ling.domain.user.event;

import java.time.LocalDateTime;

/**
 * 领域事件基础接口
 * @Author: LingRJ
 * @Description: 领域事件基础接口，所有领域事件都应实现此接口
 * @DateTime: 2025/7/11
 */
public interface DomainEvent {
    
    /**
     * 事件发生时间
     * @return 事件发生的时间戳
     */
    LocalDateTime occurredOn();
    
    /**
     * 事件类型
     * @return 事件类型标识
     */
    String eventType();
    
    /**
     * 事件版本
     * @return 事件版本号，用于事件演化
     */
    default String version() {
        return "1.0";
    }
    
    /**
     * 事件ID
     * @return 事件唯一标识
     */
    default String eventId() {
        return java.util.UUID.randomUUID().toString();
    }
}
