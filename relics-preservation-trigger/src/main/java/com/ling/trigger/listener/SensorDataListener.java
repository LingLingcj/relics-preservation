package com.ling.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import com.ling.domain.sensor.service.ISensorMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: LingRJ
 * @Description: 传感器数据MQTT监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class SensorDataListener {

    @Autowired
    private ISensorMessageService sensorMessageService;
    
    // 消息计数器和上次日志时间
    private final AtomicInteger messageCounter = new AtomicInteger(0);
    private volatile long lastLogTime = System.currentTimeMillis();
    // 每分钟记录一次统计日志
    private static final long LOG_INTERVAL_MS = 60000;

    /**
     * 监听传感器MQTT消息
     * @param message MQTT消息
     */
    @ServiceActivator(inputChannel = "sensorChannel")
    public void handleMessage(Message<?> message) {
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        String payload = message.getPayload().toString();
        
        // 增加消息计数
        messageCounter.incrementAndGet();
        
        // 仅在DEBUG级别记录详细消息内容
        log.debug("接收到传感器消息，主题: {}, 内容: {}", topic, payload);
        
        try {
            // 解析传感器数据
            SensorMessageVO sensorMessage = parseSensorMessage(topic, payload);
            if (sensorMessage != null) {
                // 将消息传递给传感器消息服务
                sensorMessageService.processSensorMessage(topic, payload);
            }
            
            // 定期记录统计信息
            logMessageStats();
            
        } catch (Exception e) {
            log.error("处理传感器消息失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 定期记录消息统计信息
     */
    private void logMessageStats() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastLogTime;
        
        // 每分钟记录一次统计信息
        if (timePassed >= LOG_INTERVAL_MS) {
            int count = messageCounter.getAndSet(0);
            double messagesPerSecond = count * 1000.0 / timePassed;
            log.info("传感器消息统计: 最近{}秒接收{}条消息, 平均每秒{}条", 
                    timePassed/1000, count, String.format("%.2f", messagesPerSecond));
            lastLogTime = currentTime;
        }
    }
    
    /**
     * 解析传感器消息
     * @param topic 主题
     * @param payload 消息内容
     * @return 传感器消息对象
     */
    private SensorMessageVO parseSensorMessage(String topic, String payload) {
        // 创建传感器消息对象
        SensorMessageVO sensorMessage = new SensorMessageVO();
        sensorMessage.setTimestamp(LocalDateTime.now());
        
        // 从topic中提取传感器信息 (格式为: light_intensity_1)
        String[] topicParts = topic.split("_");
        if (topicParts.length >= 2) {
            // 查找最后一个下划线的位置
            int lastUnderscoreIndex = topic.lastIndexOf("_");
            if (lastUnderscoreIndex > 0) {
                // 传感器类型为最后一个下划线之前的所有内容
                String sensorType = topic.substring(0, lastUnderscoreIndex);
                sensorMessage.setSensorType(sensorType);
                
                // 传感器ID为最后一个下划线之后的内容
                String sensorId = topic.substring(lastUnderscoreIndex + 1);
                sensorMessage.setSensorId(sensorId);
            } else {
                // 如果没有下划线，使用整个topic作为传感器类型
                sensorMessage.setSensorType(topic);
                sensorMessage.setSensorId("default");
            }
        } else {
            // 如果topic格式不符合预期，使用整个topic作为传感器类型
            sensorMessage.setSensorType(topic);
            sensorMessage.setSensorId("default");
        }
        
        // 解析payload中的值 - 处理包含多个字段的JSON格式
        try {
            com.alibaba.fastjson2.JSONObject jsonObj = JSON.parseObject(payload);
            
            // 遍历JSON对象中的所有字段
            boolean valueFound = false;
            for (String key : jsonObj.keySet()) {
                // 根据字段名称设置相应的值
                if ("intensity".equalsIgnoreCase(key) ||
                    "temperature".equalsIgnoreCase(key) ||
                    "humidity".equalsIgnoreCase(key) ||
                    "light".equalsIgnoreCase(key) ||
                    "pressure".equalsIgnoreCase(key)) {
                    
                    sensorMessage.setValue(jsonObj.getDouble(key));
                    log.debug("从{}字段中解析到值: {}", key, sensorMessage.getValue());
                    valueFound = true;
                    break;  // 找到第一个匹配的字段后退出
                }
            }
            
            // 如果没有找到匹配的字段，尝试直接解析为数值
            if (!valueFound) {
                try {
                    sensorMessage.setValue(Double.parseDouble(payload.trim()));
                } catch (NumberFormatException nfe) {
                    log.warn("无法解析传感器消息值: {}", payload);
                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("解析传感器消息时发生异常: {}", e.getMessage());
            return null;
        }
        
        // 验证必要字段
        if (sensorMessage.getValue() == null) {
            log.warn("传感器值为空，无法处理");
            return null;
        }
        
        log.debug("成功解析传感器数据: 类型={}, ID={}, 值={}", 
                sensorMessage.getSensorType(), sensorMessage.getSensorId(), sensorMessage.getValue());
        return sensorMessage;
    }
} 