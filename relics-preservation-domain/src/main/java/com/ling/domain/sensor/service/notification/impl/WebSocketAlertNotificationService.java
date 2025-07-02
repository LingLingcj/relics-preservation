package com.ling.domain.sensor.service.notification.impl;

import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.service.notification.AlertNotificationService;
import com.ling.domain.sensor.service.notification.model.AlertNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: LingRJ
 * @Description: WebSocket报警通知服务实现
 * @DateTime: 2025/7/2
 */
@Service
public class WebSocketAlertNotificationService implements AlertNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // 用于存储最近发送的通知，键为 "sensorId:alertType"
    private final Map<String, AlertNotificationRecord> lastNotificationMap = new ConcurrentHashMap<>();
    
    // 通知冷却时间（分钟）
    private static final int NOTIFICATION_COOLDOWN_MINUTES = 0;

    @Override
    public void sendAlertNotification(AlertNotification alertNotification) {
        if (shouldSendNotification(alertNotification)) {
            // 发送WebSocket消息到特定主题
            messagingTemplate.convertAndSend("/topic/alerts", alertNotification);
            
            // 更新最近通知记录
            String key = alertNotification.getSensorId() + ":" + alertNotification.getAlertType();
            lastNotificationMap.put(key, new AlertNotificationRecord(
                    alertNotification.getStatus(), 
                    LocalDateTime.now()
            ));
        }
    }

    @Override
    public AlertNotification convertFromAlertMessage(AlertMessageVO alertMessage) {
        return AlertNotification.builder()
                .sensorId(alertMessage.getSensorId())
                .relicsId(alertMessage.getRelicsId())
                .alertType(alertMessage.getAlertType())
                .message(alertMessage.getMessage())
                .value(alertMessage.getCurrentValue())
                .threshold(alertMessage.getThreshold())
                .status("ACTIVE")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public boolean shouldSendNotification(AlertNotification notification) {
        String key = notification.getSensorId() + ":" + notification.getAlertType();
        
        // 检查是否存在上一次的通知记录
        if (!lastNotificationMap.containsKey(key)) {
            return true; // 首次通知，应该发送
        }
        
        AlertNotificationRecord lastRecord = lastNotificationMap.get(key);
        
        // 如果状态发生变化，应该发送
        if (!lastRecord.status.equals(notification.getStatus())) {
            return true;
        }
        
        // 如果在冷却期内，不发送
        return lastRecord.timestamp.plusMinutes(NOTIFICATION_COOLDOWN_MINUTES)
                .isBefore(LocalDateTime.now());
    }
    
    // 内部类，用于记录最近一次通知
    private static class AlertNotificationRecord {
        private final String status;
        private final LocalDateTime timestamp;
        
        public AlertNotificationRecord(String status, LocalDateTime timestamp) {
            this.status = status;
            this.timestamp = timestamp;
        }
    }
} 