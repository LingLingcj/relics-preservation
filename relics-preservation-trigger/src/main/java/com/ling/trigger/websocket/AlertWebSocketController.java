package com.ling.trigger.websocket;

import com.ling.domain.sensor.service.notification.model.AlertNotification;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * @Author: LingRJ
 * @Description: WebSocket告警控制器
 * @DateTime: 2025/7/2
 */
@Controller
public class AlertWebSocketController {

    /**
     * 手动发送测试告警（用于前端开发测试）
     * @param notification 告警通知
     * @return 告警通知
     */
    @MessageMapping("/alert-test")
    @SendTo("/topic/alerts")
    public AlertNotification sendTestAlert(AlertNotification notification) {
        notification.setStatus("TEST");
        return notification;
    }
} 