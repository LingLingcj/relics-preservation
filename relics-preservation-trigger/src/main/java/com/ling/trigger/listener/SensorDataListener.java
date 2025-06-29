package com.ling.trigger.listener;

import com.alibaba.fastjson2.JSON;
import com.ling.domain.sensor.model.valobj.SensorMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: LingRJ
 * @Description: 传感器数据MQTT监听器
 * @DateTime: 2025/6/28
 **/
@Component
@Slf4j
public class SensorDataListener {

    // 传感器类型处理器映射
    private final Map<String, Consumer<SensorMessageVO>> sensorProcessors = new HashMap<>();
    
    // 主题匹配模式
    private static final Pattern SENSOR_TOPIC_PATTERN = Pattern.compile("^sensor/([^/]+)/([^/]+)$");
    private static final Pattern TYPE_ID_PATTERN = Pattern.compile("^(temperature|humidity|light)_(.+)$");
    
    public SensorDataListener() {
        // 初始化处理器映射
        sensorProcessors.put("temperature", this::processTemperatureData);
        sensorProcessors.put("humidity", this::processHumidityData);
        sensorProcessors.put("light", this::processLightData);
        sensorProcessors.put("movement", this::processMovementData);
    }

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
            SensorMessageVO sensorMessage = parseSensorMessage(topic, payload);
            if (sensorMessage != null) {
                processSensorData(sensorMessage);
            }
        } catch (Exception e) {
            log.error("处理传感器消息失败: {}", e.getMessage(), e);
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
        
        // 1. 尝试从JSON解析完整的传感器消息
        try {
            SensorMessageVO parsedMessage = JSON.parseObject(payload, SensorMessageVO.class);
            if (parsedMessage != null) {
                // 合并解析结果与默认值
                if (parsedMessage.getSensorId() != null && !parsedMessage.getSensorId().isEmpty()) {
                    sensorMessage.setSensorId(parsedMessage.getSensorId());
                }
                if (parsedMessage.getSensorType() != null && !parsedMessage.getSensorType().isEmpty()) {
                    sensorMessage.setSensorType(parsedMessage.getSensorType());
                }
                if (parsedMessage.getValue() != null) {
                    sensorMessage.setValue(parsedMessage.getValue());
                }
                if (parsedMessage.getTimestamp() != null) {
                    sensorMessage.setTimestamp(parsedMessage.getTimestamp());
                }
            }
        } catch (Exception e) {
            log.debug("JSON解析为SensorMessageVO失败，尝试其他解析方式: {}", e.getMessage());
        }
        
        // 2. 从主题中解析传感器ID和类型
        extractSensorInfoFromTopic(topic, sensorMessage);
        
        // 3. 如果值为空，尝试从payload中解析值
        if (sensorMessage.getValue() == null) {
            extractValueFromPayload(payload, sensorMessage);
        }
        
        // 4. 验证必要字段
        if (sensorMessage.getSensorId() == null || sensorMessage.getSensorId().isEmpty()) {
            sensorMessage.setSensorId("default");
        }
        
        if (sensorMessage.getSensorType() == null || sensorMessage.getSensorType().isEmpty()) {
            log.warn("无法确定传感器类型，主题: {}, 内容: {}", topic, payload);
            return null;
        }
        
        log.info("成功解析传感器数据: 类型={}, ID={}, 值={}", 
                sensorMessage.getSensorType(), sensorMessage.getSensorId(), sensorMessage.getValue());
        return sensorMessage;
    }
    
    /**
     * 从主题中提取传感器信息
     * @param topic 主题
     * @param sensorMessage 传感器消息对象
     */
    private void extractSensorInfoFromTopic(String topic, SensorMessageVO sensorMessage) {
        // 1. 处理标准格式: sensor/id/type
        Matcher sensorMatcher = SENSOR_TOPIC_PATTERN.matcher(topic);
        if (sensorMatcher.matches()) {
            sensorMessage.setSensorId(sensorMatcher.group(1));
            sensorMessage.setSensorType(sensorMatcher.group(2));
            return;
        }
        
        // 2. 处理type_id格式: temperature_123, humidity_abc, light_intensity_xyz
        Matcher typeMatcher = TYPE_ID_PATTERN.matcher(topic);
        if (typeMatcher.matches()) {
            sensorMessage.setSensorType(typeMatcher.group(1));
            sensorMessage.setSensorId(typeMatcher.group(2));
            return;
        }
        
        // 3. 处理简单主题: light, temperature, humidity
        if (topic.equals("light") || topic.equals("temperature") || topic.equals("humidity")) {
            sensorMessage.setSensorType(topic);
            sensorMessage.setSensorId("default");
            return;
        }
        
        // 4. 尝试从主题中推断类型
        if (topic.contains("temp")) {
            sensorMessage.setSensorType("temperature");
        } else if (topic.contains("hum")) {
            sensorMessage.setSensorType("humidity");
        } else if (topic.contains("light")) {
            sensorMessage.setSensorType("light");
        }
        
        // 尝试从主题中提取数字作为ID
        if (sensorMessage.getSensorId() == null || sensorMessage.getSensorId().isEmpty()) {
            Pattern idPattern = Pattern.compile("\\d+");
            Matcher idMatcher = idPattern.matcher(topic);
            if (idMatcher.find()) {
                sensorMessage.setSensorId(idMatcher.group());
            }
        }
    }
    
    /**
     * 从消息内容中提取传感器值
     * @param payload 消息内容
     * @param sensorMessage 传感器消息对象
     */
    private void extractValueFromPayload(String payload, SensorMessageVO sensorMessage) {
        // 1. 尝试解析为JSON
        try {
            com.alibaba.fastjson2.JSONObject jsonObject = JSON.parseObject(payload);
            
            // 尝试从JSON中获取值
            String sensorType = sensorMessage.getSensorType();
            if (jsonObject.containsKey(sensorType)) {
                sensorMessage.setValue(jsonObject.getDouble(sensorType));
                return;
            }
            
            // 尝试从常见字段中获取值
            if (jsonObject.containsKey("value")) {
                sensorMessage.setValue(jsonObject.getDouble("value"));
                return;
            }
            
            if (jsonObject.containsKey("intensity") && "light".equals(sensorType)) {
                sensorMessage.setValue(jsonObject.getDouble("intensity"));
                return;
            }
        } catch (Exception e) {
            log.debug("JSON解析失败，尝试直接解析为数值: {}", e.getMessage());
        }
        
        // 2. 尝试直接解析为数值
        try {
            sensorMessage.setValue(Double.parseDouble(payload.trim()));
        } catch (NumberFormatException e) {
            log.debug("无法将payload解析为数值: {}", payload);
        }
    }
    
    /**
     * 处理传感器数据
     * @param sensorMessage 传感器消息
     */
    private void processSensorData(SensorMessageVO sensorMessage) {
        // 根据传感器类型选择对应的处理器
        Consumer<SensorMessageVO> processor = sensorProcessors.get(sensorMessage.getSensorType());
        if (processor != null) {
            processor.accept(sensorMessage);
        } else {
            log.info("处理未知类型传感器数据: {}", sensorMessage);
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