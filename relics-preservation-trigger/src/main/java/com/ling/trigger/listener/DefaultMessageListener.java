package com.ling.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @Author: LingRJ
 * @Description: 默认MQTT消息监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class DefaultMessageListener {

    /**
     * 处理未被特定监听器处理的MQTT消息
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "defaultChannel")
    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();
        
        log.info("接收到未分类MQTT消息，主题: {}, 内容: {}", topic, payload);
        
        // 这里可以实现未分类消息的通用处理逻辑，如记录日志、转发到其他系统等
    }
} 