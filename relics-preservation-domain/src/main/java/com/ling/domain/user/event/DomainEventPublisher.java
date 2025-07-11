package com.ling.domain.user.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器
 * @Author: LingRJ
 * @Description: 负责发布领域事件到Spring事件总线
 * @DateTime: 2025/7/11
 */
@Component
@Slf4j
public class DomainEventPublisher {
    
    private static ApplicationEventPublisher eventPublisher;
    
    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        DomainEventPublisher.eventPublisher = eventPublisher;
    }
    
    /**
     * 发布领域事件
     * @param event 要发布的领域事件
     */
    public static void publish(DomainEvent event) {
        if (eventPublisher != null) {
            try {
                log.debug("发布领域事件: {} - {}", event.eventType(), event.eventId());
                eventPublisher.publishEvent(event);
            } catch (Exception e) {
                log.error("发布领域事件失败: {} - {}", event.eventType(), e.getMessage(), e);
            }
        } else {
            log.warn("事件发布器未初始化，无法发布事件: {}", event.eventType());
        }
    }
    
    /**
     * 批量发布领域事件
     * @param events 要发布的领域事件列表
     */
    public static void publishAll(DomainEvent... events) {
        for (DomainEvent event : events) {
            publish(event);
        }
    }
}
