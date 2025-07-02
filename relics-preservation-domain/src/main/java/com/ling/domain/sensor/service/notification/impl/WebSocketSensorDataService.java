package com.ling.domain.sensor.service.notification.impl;

import com.ling.domain.sensor.service.notification.NotificationService;
import com.ling.domain.sensor.service.notification.model.SensorNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author: LingRJ
 * @Description: WebSocket传感器数据发送服务
 *               用于将传感器数据实时推送给前端
 * @DateTime: 2025/7/2 15:04
 *
 */
@Service
public class WebSocketSensorDataService implements NotificationService<SensorNotification> {

    private final SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    public WebSocketSensorDataService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    @Override
    public void send(SensorNotification notification) {
        // 根据传感器类型发送到不同的主题
        String destination = String.format("/topic/sensor-data/%s", notification.getSensorType().toLowerCase());
        messagingTemplate.convertAndSend(destination, notification);
        
        // 同时发送到一个汇总的主题，包含所有传感器数据
        messagingTemplate.convertAndSend("/topic/sensor-data/all", notification);
    }
} 