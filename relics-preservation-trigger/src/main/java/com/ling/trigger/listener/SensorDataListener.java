package com.ling.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 传感器数据MQTT监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class SensorDataListener {

    /**
     * 监听传感器MQTT消息
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "sensorChannel")
    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();
        
        log.info("接收到传感器消息，主题: {}, 内容: {}", topic, payload);
        
        try {
            // 解析传感器数据
            processSensorData(topic, payload);
        } catch (Exception e) {
            log.error("处理传感器消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理传感器数据
     * @param topic 主题
     * @param payload 消息内容
     */
    private void processSensorData(String topic, String payload) {
        // 解析传感器ID
        String[] topicParts = topic.split("/");
        if (topicParts.length >= 3) {
            String sensorId = topicParts[1];
            log.info("处理传感器 [{}] 数据", sensorId);
            
            try {
                // 解析传感器消息
                SensorMessageVO sensorMessage = JSON.parseObject(payload, SensorMessageVO.class);
                
                // 如果传感器ID为空，则使用主题中的ID
                if (sensorMessage.getSensorId() == null || sensorMessage.getSensorId().isEmpty()) {
                    sensorMessage.setSensorId(sensorId);
                }
                
                // 如果时间戳为空，则使用当前时间
                if (sensorMessage.getTimestamp() == null) {
                    sensorMessage.setTimestamp(LocalDateTime.now());
                }
                
                // 这里可以根据不同的传感器类型进行不同的处理
                switch (sensorMessage.getSensorType()) {
                    case "temperature":
                        processTemperatureData(sensorMessage);
                        break;
                    case "humidity":
                        processHumidityData(sensorMessage);
                        break;
                    case "light":
                        processLightData(sensorMessage);
                        break;
                    case "movement":
                        processMovementData(sensorMessage);
                        break;
                    default:
                        log.info("处理未知类型传感器数据: {}", sensorMessage);
                        break;
                }
            } catch (Exception e) {
                log.error("解析传感器数据失败: {}", e.getMessage(), e);
            }
        }
    }
    
    private void processTemperatureData(SensorMessageVO sensorMessage) {
        log.info("处理温度传感器数据: {}", sensorMessage);
        // 温度传感器处理逻辑
    }
    
    private void processHumidityData(SensorMessageVO sensorMessage) {
        log.info("处理湿度传感器数据: {}", sensorMessage);
        // 湿度传感器处理逻辑
    }
    
    private void processLightData(SensorMessageVO sensorMessage) {
        log.info("处理光照传感器数据: {}", sensorMessage);
        // 光照传感器处理逻辑
    }
    
    private void processMovementData(SensorMessageVO sensorMessage) {
        log.info("处理移动传感器数据: {}", sensorMessage);
        // 移动传感器处理逻辑
    }
} 