package com.ling.trigger.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * 传感器数据WebSocket控制器
 * 负责处理WebSocket连接和消息路由
 * @author 31229
 */
@Controller
public class SensorDataWebSocketController {

    /**
     * 处理从客户端发来的订阅消息
     * 此方法主要用于确认客户端连接
     * 
     * @param message 客户端消息
     * @return 确认消息
     */
    @MessageMapping("/subscribe-sensor-data")
    @SendTo("/topic/sensor-data/confirmation")
    public String handleSubscription(String message) {
        return "已成功订阅传感器数据";
    }
    
    // 注意：实际的数据发送将通过WebSocketSensorDataService完成
} 