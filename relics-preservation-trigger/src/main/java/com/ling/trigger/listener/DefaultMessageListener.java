package com.ling.trigger.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @Author: LingRJ
 * @Description: 默认MQTT消息监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class DefaultMessageListener {

    @Autowired
    private MessageChannel sensorChannel;

    /**
     * 处理未被特定监听器处理的MQTT消息
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "defaultChannel")
    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();
        
        log.info("接收到未分类MQTT消息，主题: {}, 内容: {}", topic, payload);
        
        // 打印所有头信息，帮助调试
        log.debug("消息头信息:");
        message.getHeaders().forEach((key, value) -> {
            log.debug("  {} = {}", key, value);
        });
        
        // 检查主题格式，如果可能是传感器消息，转发到传感器通道
        boolean isSensorMessage = false;
        
        // 根据主题名称判断是否可能是传感器消息
        if (topic.contains("light") || topic.contains("temperature") || topic.contains("humidity") || 
            topic.contains("sensor") || topic.contains("intensity") || 
            topic.matches(".*\\d+.*")) { // 包含数字的主题可能是传感器ID
            
            log.info("检测到可能是传感器消息，转发到传感器通道处理。主题: {}", topic);
            isSensorMessage = true;
        }
        
        // 根据消息内容判断是否可能是传感器消息
        if (!isSensorMessage && payload.contains("{") && payload.contains("}")) {
            // 检查JSON内容是否包含传感器相关字段
            if (payload.contains("\"value\"") || payload.contains("\"intensity\"") || 
                payload.contains("\"temperature\"") || payload.contains("\"humidity\"")) {
                
                log.info("根据消息内容判断可能是传感器消息，转发到传感器通道处理。主题: {}", topic);
                isSensorMessage = true;
            }
        }
        
        // 如果可能是传感器消息，转发到传感器通道
        if (isSensorMessage) {
            try {
                // 创建新消息并保留原始头信息
                Message<?> newMessage = MessageBuilder.fromMessage(message)
                        .build();
                
                // 发送到传感器通道
                sensorChannel.send(newMessage);
                log.info("已将消息转发到传感器通道: {}", topic);
            } catch (Exception e) {
                log.error("转发消息到传感器通道失败: {}", e.getMessage(), e);
            }
        } else {
            log.info("未识别为传感器消息，不进行转发: {}", topic);
        }
    }
} 