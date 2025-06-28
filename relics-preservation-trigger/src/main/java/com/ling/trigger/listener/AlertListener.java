package com.ling.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.sensor.model.valobj.AlertMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @Author: LingRJ
 * @Description: 告警MQTT监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class AlertListener {

    /**
     * 监听告警MQTT消息
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "alertChannel")
    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();
        
        log.info("接收到告警消息，主题: {}, 内容: {}", topic, payload);
        
        try {
            // 处理告警消息
            processAlertData(topic, payload);
        } catch (Exception e) {
            log.error("处理告警消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理告警数据
     * @param topic 主题
     * @param payload 消息内容
     */
    private void processAlertData(String topic, String payload) {
        // 根据不同告警类型进行处理
        String[] topicParts = topic.split("/");
        if (topicParts.length >= 3) {
            String alertType = topicParts[1];
            String targetId = topicParts[2];
            
            log.info("处理告警 [{}] 数据，目标ID: {}", alertType, targetId);
            
            try {
                // 解析告警消息
                AlertMessageVO alertMessage = JSON.parseObject(payload, AlertMessageVO.class);
                
                // 如果告警类型为空，则使用主题中的类型
                if (alertMessage.getAlertType() == null || alertMessage.getAlertType().isEmpty()) {
                    alertMessage.setAlertType(alertType);
                }
                
                // 如果时间戳为空，则使用当前时间
                if (alertMessage.getTimestamp() == null) {
                    alertMessage.setTimestamp(LocalDateTime.now());
                }
                
                // 如果告警ID为空，则生成一个
                if (alertMessage.getAlertId() == null || alertMessage.getAlertId().isEmpty()) {
                    alertMessage.setAlertId(java.util.UUID.randomUUID().toString());
                }
                
                // 根据告警严重程度和类型分别处理
                switch (alertMessage.getSeverity()) {
                    case "CRITICAL":
                        processCriticalAlert(alertMessage);
                        break;
                    case "WARNING":
                        processWarningAlert(alertMessage);
                        break;
                    case "INFO":
                        processInfoAlert(alertMessage);
                        break;
                    default:
                        // 使用默认的处理方式，按类型处理
                        processAlertByType(alertMessage);
                        break;
                }
            } catch (Exception e) {
                log.error("解析告警数据失败: {}", e.getMessage(), e);
            }
        }
    }
    
    private void processCriticalAlert(AlertMessageVO alertMessage) {
        log.info("处理严重告警: {}", alertMessage);
        // 严重告警处理逻辑：
        // 1. 发送紧急通知
        // 2. 触发应急预案
        // 3. 记录告警事件
    }
    
    private void processWarningAlert(AlertMessageVO alertMessage) {
        log.info("处理警告告警: {}", alertMessage);
        // 警告告警处理逻辑：
        // 1. 发送告警通知
        // 2. 记录告警事件
    }
    
    private void processInfoAlert(AlertMessageVO alertMessage) {
        log.info("处理信息告警: {}", alertMessage);
        // 信息告警处理逻辑：
        // 1. 记录告警事件
    }
    
    private void processAlertByType(AlertMessageVO alertMessage) {
        // 按照告警类型处理
        switch (alertMessage.getAlertType()) {
            case "temperature":
                processTemperatureAlert(alertMessage);
                break;
            case "humidity":
                processHumidityAlert(alertMessage);
                break;
            case "light":
                processLightAlert(alertMessage);
                break;
            case "security":
                processSecurityAlert(alertMessage);
                break;
            default:
                log.warn("未知的告警类型: {}", alertMessage.getAlertType());
        }
    }
    
    private void processTemperatureAlert(AlertMessageVO alertMessage) {
        log.info("处理温度告警: {}", alertMessage);
        // 实现温度告警处理逻辑
    }
    
    private void processHumidityAlert(AlertMessageVO alertMessage) {
        log.info("处理湿度告警: {}", alertMessage);
        // 实现湿度告警处理逻辑
    }
    
    private void processLightAlert(AlertMessageVO alertMessage) {
        log.info("处理光照告警: {}", alertMessage);
        // 实现光照告警处理逻辑
    }
    
    private void processSecurityAlert(AlertMessageVO alertMessage) {
        log.info("处理安全告警: {}", alertMessage);
        // 实现安全告警处理逻辑
    }
} 