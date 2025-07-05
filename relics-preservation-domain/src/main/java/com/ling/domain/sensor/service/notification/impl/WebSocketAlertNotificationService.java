package com.ling.domain.sensor.service.notification.impl;

import com.ling.domain.sensor.adapter.IAlertRecordRepository;
import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import com.ling.domain.sensor.service.notification.NotificationService;
import com.ling.domain.sensor.service.notification.model.AlertNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: LingRJ
 * @Description: WebSocket告警通知服务
 * @DateTime: 2025/7/2
 */
@Service
public class WebSocketAlertNotificationService implements NotificationService<AlertNotification> {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final IAlertRecordRepository alertRecordRepository;
    
    @Autowired
    public WebSocketAlertNotificationService(
            SimpMessagingTemplate messagingTemplate,
            IAlertRecordRepository alertRecordRepository) {
        this.messagingTemplate = messagingTemplate;
        this.alertRecordRepository = alertRecordRepository;
    }
    
    // 用于存储最近发送的通知，键为 "sensorId:alertType"
    private final Map<String, AlertNotificationRecord> lastNotificationMap = new ConcurrentHashMap<>();
    
    // 通知冷却时间（分钟）
    private static final int NOTIFICATION_COOLDOWN_MINUTES = 0;

    @Override
    public void send(AlertNotification notification) {
        sendAlertNotification(notification);
    }
    
    @Override
    public void sendAlertNotification(AlertNotification alertNotification) {
        if (!shouldSendNotification(alertNotification)) {
            return;
        }
        
        // 持久化告警记录
        persistAlertNotification(alertNotification);
        
        // 发送WebSocket消息
        messagingTemplate.convertAndSend("/topic/alerts", alertNotification);
    }
    
    @Override
    public AlertNotification convertFromAlertMessage(AlertMessageVO alertMessage) {
        AlertNotification notification = new AlertNotification();
        notification.setAlertType(alertMessage.getAlertType());
        notification.setMessage(alertMessage.getMessage());
        notification.setTimestamp(alertMessage.getTimestamp());
        notification.setSensorId(alertMessage.getSensorId());
        notification.setRelicsId(alertMessage.getRelicsId());
        notification.setValue(alertMessage.getCurrentReading());
        notification.setThreshold(alertMessage.getThreshold());
        notification.setStatus("ACTIVE");
        return notification;
    }
    
    @Override
    public boolean shouldSendNotification(AlertNotification alertNotification) {
        // 实现通知发送条件，例如频率限制、紧急程度等
        return true;
    }
    
    /**
     * 持久化告警通知
     * @param alertNotification 告警通知
     */
    private void persistAlertNotification(AlertNotification alertNotification) {
        // 检查是否已存在活跃的告警，如果不存在则持久化
        if (!alertRecordRepository.existsActiveAlert(
                alertNotification.getSensorId(), alertNotification.getAlertType())) {
            alertRecordRepository.saveAlertNotification(alertNotification);
        }
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
